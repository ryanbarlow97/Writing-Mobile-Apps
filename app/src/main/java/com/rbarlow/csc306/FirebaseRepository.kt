package com.rbarlow.csc306

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlin.math.min

class FirebaseRepository {

    private val firebaseInstance =
        FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")


    //returns a string of the users role, either curator or user
    fun getUserRole(userId: String): MutableLiveData<String?> {
        val liveData = MutableLiveData<String?>()

        firebaseInstance.reference.child("users").child(userId).child("role")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val role = dataSnapshot.getValue(String::class.java)
                    liveData.value = role
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve user role", databaseError.toException())
                }
            })

        return liveData
    }

    //get all the categories for the user to see
    fun getCategories(userId: String): MutableLiveData<List<Category>> {
        val liveData = MutableLiveData<List<Category>>()

        firebaseInstance.reference.child("users").child(userId).child("user_categories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val categories = mutableListOf<Category>()
                    for (categorySnapshot in dataSnapshot.children) {
                        val categoryTitle = categorySnapshot.getValue(String::class.java)
                        categoryTitle?.let {
                            val category = Category(it)
                            categories.add(category)
                        }
                    }
                    liveData.value = categories
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve categories", databaseError.toException())
                }
            })
        return liveData
    }

    fun getAllItems(): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()

        firebaseInstance.reference.child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    for (itemSnapshot in dataSnapshot.children) {
                        val itemId = itemSnapshot.key
                        val name = itemSnapshot.child("name").getValue(String::class.java)
                        val description = itemSnapshot.child("description").getValue(String::class.java)
                        val image = itemSnapshot.child("image").getValue(String::class.java)
                        val addedBy = itemSnapshot.child("addedBy").getValue(String::class.java)
                        val addedOn = itemSnapshot.child("addedOn").getValue(Long::class.java)

                        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null) {
                            val item = Item(itemId, name, description, image, addedOn, addedBy)
                            items.add(item)
                        }
                    }
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve items", databaseError.toException())
                }
            })

        return liveData
    }
    fun getItem(itemName: String): MutableLiveData<Item> {
        val liveData = MutableLiveData<Item>()

        firebaseInstance.reference.child("items")
            .orderByChild("name")
            .equalTo(itemName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (itemSnapshot in dataSnapshot.children) {
                        val itemId = itemSnapshot.key
                        val name = itemSnapshot.child("name").getValue(String::class.java)
                        val description = itemSnapshot.child("description").getValue(String::class.java)
                        val image = itemSnapshot.child("image").getValue(String::class.java)
                        val addedBy = itemSnapshot.child("addedBy").getValue(String::class.java)
                        val addedOn = itemSnapshot.child("addedOn").getValue(Long::class.java)
                        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null) {
                            val item = Item(itemId, name, description, image, addedOn, addedBy)
                            liveData.value = item
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve item", databaseError.toException())
                }
            })
        return liveData
    }

    //bookmark items for the user
    fun bookmarkItem(user: FirebaseUser, itemId: String) {
        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")

        userBookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(itemId)) {
                    // Item is already bookmarked, remove the bookmark
                    userBookmarksRef.child(itemId).removeValue()
                } else {
                    // Item is not bookmarked, add the bookmark
                    userBookmarksRef.child(itemId).setValue(true)
                    userBookmarksRef.child(itemId).child("addedOn").setValue(ServerValue.TIMESTAMP)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not update bookmarks", databaseError.toException())
            }
        })
    }

    //get all the items that the user has bookmarked
    fun getBookmarkedItems(user: FirebaseUser): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()

        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")
        userBookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                val itemCount = dataSnapshot.childrenCount
                if (itemCount == 0L) {
                    liveData.value = items
                } else {
                    var processedItems = 0L
                    for (itemSnapshot in dataSnapshot.children) {
                        val itemId = itemSnapshot.key
                        val addedOn = itemSnapshot.child("addedOn").getValue(Long::class.java)
                        if (itemId != null && addedOn != null) {
                            firebaseInstance.reference.child("items").child(itemId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val name = dataSnapshot.child("name").getValue(String::class.java)
                                        val fullDescription = dataSnapshot.child("description").getValue(String::class.java)
                                        val description = fullDescription?.substring(0, min(fullDescription.length, 30)) + "..."
                                        val image = dataSnapshot.child("image").getValue(String::class.java)
                                        val addedBy = dataSnapshot.child("addedBy").getValue(String::class.java)
                                        if (name != null && description != null && image != null && addedBy != null) {
                                            val item = Item(itemId, name, description, image, addedOn, addedBy)
                                            items.add(item)
                                        }
                                        processedItems++
                                        if (processedItems == itemCount) {
                                            liveData.value = items
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Log.e("Firebase", "Could not retrieve bookmarked items", databaseError.toException())
                                    }
                                })
                        } else {
                            processedItems++
                            if (processedItems == itemCount) {
                                liveData.value = items
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not retrieve bookmarked items", databaseError.toException())
            }
        })

        return liveData
    }


    fun isItemBookmarked(user: FirebaseUser, itemId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()

        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")
        userBookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                liveData.value = dataSnapshot.hasChild(itemId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not check if item is bookmarked", databaseError.toException())
            }
        })

        return liveData
    }

}