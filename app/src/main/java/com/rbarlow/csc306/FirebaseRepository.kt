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
    fun getCategories(): MutableLiveData<List<Category>> {
        val liveData = MutableLiveData<List<Category>>()

        firebaseInstance.reference.child("categories")
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
                        val views = itemSnapshot.child("views").getValue(Int::class.java)

                        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
                            val item = Item(itemId, name, description, image, addedOn, addedBy, views)
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
    fun getItem(itemId: String): MutableLiveData<Item> {
        val liveData = MutableLiveData<Item>()

        firebaseInstance.reference.child("items")
            .child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val itemId2 = dataSnapshot.key
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    val description = dataSnapshot.child("description").getValue(String::class.java)
                    val image = dataSnapshot.child("image").getValue(String::class.java)
                    val addedBy = dataSnapshot.child("addedBy").getValue(String::class.java)
                    val addedOn = dataSnapshot.child("addedOn").getValue(Long::class.java)
                    val views = dataSnapshot.child("views").getValue(Int::class.java)
                    if (itemId2 != null && name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
                        val item = Item(itemId2, name, description, image, addedOn, addedBy, views)
                        liveData.value = item
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
                                        val views = dataSnapshot.child("views").getValue(Int::class.java)
                                        if (name != null && fullDescription != null && image != null && addedBy != null && addedOn != null && views != null) {
                                            val item = Item(itemId, name, description, image, addedOn, addedBy , views)
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

    fun getNewestItems(limit: Int = 6): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()

        firebaseInstance.reference.child("items")
            .orderByChild("addedOn")
            .limitToLast(limit)
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
                        val views = itemSnapshot.child("views").getValue(Int::class.java)

                        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
                            val item = Item(itemId, name, description, image, addedOn, addedBy, views)
                            items.add(item)
                        }
                    }
                    items.sortByDescending { it.addedOn } // Sort items by addedOn in descending order
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve newest items", databaseError.toException())
                }
            })

        return liveData
    }

    //get the most viewed items
    fun getMostViewedItems(limit: Int = 6): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()

        firebaseInstance.reference.child("items")
            .orderByChild("views")
            .limitToLast(limit)
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
                        val views = itemSnapshot.child("views").getValue(Int::class.java)

                        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
                            val item = Item(itemId, name, description, image, addedOn, addedBy, views)
                            items.add(item)
                        }
                    }
                    items.sortByDescending { it.views } // Sort items by views in descending order
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve most viewed items", databaseError.toException())
                }
            })

        return liveData
    }
    //add a view to an item
    fun addViewToItem(itemId: String) {
        firebaseInstance.reference.child("items").child(itemId).child("views")
            .runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val views = mutableData.getValue(Int::class.java)
                    if (views != null) {
                        mutableData.value = views + 1
                    }
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                    if (databaseError != null) {
                        Log.e("Firebase", "Could not increment item views", databaseError.toException())
                    }
                }
            })
    }
    //user has viewed the item
    fun userViewedItem(user: FirebaseUser, itemId: String) {
        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.child(itemId).setValue(true)
        userViewedItemsRef.child(itemId).child("viewedOn").setValue(ServerValue.TIMESTAMP)
    }

    //get the items that the user has viewed
    fun hasUserViewedItem(user: FirebaseUser, itemId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()

        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                liveData.value = dataSnapshot.hasChild(itemId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not check if user has viewed item", databaseError.toException())
            }
        })

        return liveData
    }

    //get list of items that the user has viewed
    fun getUserViewedItems(user: FirebaseUser): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()

        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.orderByChild("viewedOn").limitToLast(6)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    val itemCount = dataSnapshot.childrenCount
                    if (itemCount == 0L) {
                        liveData.value = items
                    } else {
                        var processedItems = 0L
                        for (itemSnapshot in dataSnapshot.children) {
                            val itemId = itemSnapshot.key
                            if (itemId != null) {
                                firebaseInstance.reference.child("items").child(itemId)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val name = dataSnapshot.child("name").getValue(String::class.java)
                                            val description = dataSnapshot.child("description").getValue(String::class.java)
                                            val image = dataSnapshot.child("image").getValue(String::class.java)
                                            val addedBy = dataSnapshot.child("addedBy").getValue(String::class.java)
                                            val addedOn = dataSnapshot.child("addedOn").getValue(Long::class.java)
                                            val views = dataSnapshot.child("views").getValue(Int::class.java)
                                            if (name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
                                                val item = Item(itemId, name, description, image, addedOn, addedBy, views)
                                                items.add(item)
                                            }
                                            processedItems++
                                            if (processedItems == itemCount) {
                                                liveData.value = items
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Log.e("Firebase", "Could not retrieve viewed items", databaseError.toException())
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
                    Log.e("Firebase", "Could not retrieve viewed items", databaseError.toException())
                }
            })
        return liveData
    }

    fun deleteItemWithReferences(itemId: String, onComplete: ((Boolean, String?) -> Unit)?) {
        // Delete all references to the item in the users' bookmarks and viewedItems
        firebaseInstance.reference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userId = userSnapshot.key
                        if (userId != null) {
                            val bookmarksRef = userSnapshot.ref.child("bookmarks").child(itemId)
                            bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        bookmarksRef.removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Error in bookmarks reference", error.toException())
                                }
                            })

                            val viewedItemsRef = userSnapshot.ref.child("viewedItems").child(itemId)
                            viewedItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        viewedItemsRef.removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Error in viewedItems reference", error.toException())
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not delete item references", databaseError.toException())
                }
            })

        // Delete the item from the items node
        firebaseInstance.reference.child("items").child(itemId).removeValue()
            .addOnCompleteListener { task ->
                onComplete?.invoke(task.isSuccessful, task.exception?.message)
            }
    }
}


