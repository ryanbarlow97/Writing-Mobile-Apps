package com.rbarlow.csc306

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlin.math.min

class FirebaseRepository {

    private val firebaseInstance =
        FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")

    //region User Role
    fun getUserRole(userId: String): MutableLiveData<String?> {
        val liveData = MutableLiveData<String?>()
        fetchUserRole(userId, liveData)
        return liveData
    }

    private fun fetchUserRole(userId: String, liveData: MutableLiveData<String?>) {
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
    }
    //endregion

    //region Categories
    fun getCategories(): MutableLiveData<List<Category>> {
        val liveData = MutableLiveData<List<Category>>()
        fetchCategories(liveData)
        return liveData
    }

    private fun fetchCategories(liveData: MutableLiveData<List<Category>>) {
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
    }
    //endregion

    //region All Items
    fun getAllItems(): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        fetchAllItems(liveData)
        return liveData
    }

    private fun fetchAllItems(liveData: MutableLiveData<List<Item>>) {
        firebaseInstance.reference.child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    for (itemSnapshot in dataSnapshot.children) {
                        val item = getItemFromSnapshot(itemSnapshot)
                        item?.let { items.add(it) }
                    }
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve items", databaseError.toException())
                }
            })
    }

    private fun getItemFromSnapshot(itemSnapshot: DataSnapshot): Item? {
        val itemId = itemSnapshot.key
        val name = itemSnapshot.child("name").getValue(String::class.java)
        val description = itemSnapshot.child("description").getValue(String::class.java)
        val image = itemSnapshot.child("image").getValue(String::class.java)
        val addedBy = itemSnapshot.child("addedBy").getValue(String::class.java)
        val addedOn = itemSnapshot.child("addedOn").getValue(Long::class.java)
        val views = itemSnapshot.child("views").getValue(Int::class.java)

        if (itemId != null && name != null && description != null && image != null && addedBy != null && addedOn != null && views != null) {
            return Item(itemId, name, description, image, addedOn, addedBy, views)
        }
        return null
    }
    //endregion

    //region Single Item
    fun getItem(itemId: String): MutableLiveData<Item> {
        val liveData = MutableLiveData<Item>()
        fetchItem(itemId, liveData)
        return liveData
    }

    private fun fetchItem(itemId: String, liveData: MutableLiveData<Item>) {
        firebaseInstance.reference.child("items")
            .child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = getItemFromSnapshot(dataSnapshot)
                    item?.let { liveData.value = it }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve item", databaseError.toException())
                }
            })
    }
    //endregion

    //region Bookmark Items
    fun bookmarkItem(user: FirebaseUser, itemId: String) {
        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")
        updateBookmarks(userBookmarksRef, itemId)
    }

    private fun updateBookmarks(userBookmarksRef: DatabaseReference, itemId: String) {
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

    fun getBookmarkedItems(user: FirebaseUser): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        fetchBookmarkedItems(user, liveData)
        return liveData
    }

    private fun fetchBookmarkedItems(user: FirebaseUser, liveData: MutableLiveData<List<Item>>) {
        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")
        userBookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                val itemCount = dataSnapshot.childrenCount
                if (itemCount == 0L) {
                    liveData.value = items
                } else {
                    processBookmarkedItems(dataSnapshot, items, liveData)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not retrieve bookmarked items", databaseError.toException())
            }
        })
    }

    private fun processBookmarkedItems(dataSnapshot: DataSnapshot, items: MutableList<Item>, liveData: MutableLiveData<List<Item>>) {
        var processedItems = 0L
        val itemCount = dataSnapshot.childrenCount
        for (itemSnapshot in dataSnapshot.children) {
            val itemId = itemSnapshot.key
            val addedOn = itemSnapshot.child("addedOn").getValue(Long::class.java)
            if (itemId != null && addedOn != null) {
                fetchItemForBookmarkedItems(itemId, items, itemCount, processedItems, liveData)
            } else {
                processedItems++
                if (processedItems == itemCount) {
                    liveData.value = items
                }
            }
        }
    }

    private fun fetchItemForBookmarkedItems(itemId: String, items: MutableList<Item>, itemCount: Long, processedItems: Long, liveData: MutableLiveData<List<Item>>) {
        firebaseInstance.reference.child("items").child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = getItemFromSnapshot(dataSnapshot)
                    item?.let { items.add(it) }
                    if (processedItems + 1 == itemCount) {
                        liveData.value = items
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve bookmarked items", databaseError.toException())
                }
            })
    }

    fun isItemBookmarked(user: FirebaseUser, itemId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        checkItemBookmarked(user, itemId, liveData)
        return liveData
    }

    private fun checkItemBookmarked(user: FirebaseUser, itemId: String, liveData: MutableLiveData<Boolean>) {
        val userBookmarksRef = firebaseInstance.reference.child("users").child(user.uid).child("bookmarks")
        userBookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                liveData.value = dataSnapshot.hasChild(itemId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not check if item is bookmarked", databaseError.toException())
            }
        })
    }
    //endregion

    //region Newest Items
    fun getNewestItems(limit: Int = 6): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        fetchNewestItems(limit, liveData)
        return liveData
    }

    private fun fetchNewestItems(limit: Int, liveData: MutableLiveData<List<Item>>) {
        firebaseInstance.reference.child("items")
            .orderByChild("addedOn")
            .limitToLast(limit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    for (itemSnapshot in dataSnapshot.children) {
                        val item = getItemFromSnapshot(itemSnapshot)
                        item?.let { items.add(it) }
                    }
                    items.sortByDescending { it.addedOn } // Sort items by addedOn in descending order
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve newest items", databaseError.toException())
                }
            })
    }
    //endregion

    //region Most Viewed Items
    fun getMostViewedItems(limit: Int = 6): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        fetchMostViewedItems(limit, liveData)
        return liveData
    }

    private fun fetchMostViewedItems(limit: Int, liveData: MutableLiveData<List<Item>>) {
        firebaseInstance.reference.child("items")
            .orderByChild("views")
            .limitToLast(limit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    for (itemSnapshot in dataSnapshot.children) {
                        val item = getItemFromSnapshot(itemSnapshot)
                        item?.let { items.add(it) }
                    }
                    items.sortByDescending { it.views } // Sort items by views in descending order
                    liveData.value = items
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve most viewed items", databaseError.toException())
                }
            })
    }
    //endregion

    //region Add View to Item
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
    //endregion

    //region User Viewed Item
    fun userViewedItem(user: FirebaseUser, itemId: String) {
        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.child(itemId).setValue(true)
        userViewedItemsRef.child(itemId).child("viewedOn").setValue(ServerValue.TIMESTAMP)
    }

    fun hasUserViewedItem(user: FirebaseUser, itemId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        checkUserViewedItem(user, itemId, liveData)
        return liveData
    }

    private fun checkUserViewedItem(user: FirebaseUser, itemId: String, liveData: MutableLiveData<Boolean>) {
        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                liveData.value = dataSnapshot.hasChild(itemId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Could not check if user has viewed item", databaseError.toException())
            }
        })
    }

    fun getUserViewedItems(user: FirebaseUser): MutableLiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        fetchUserViewedItems(user, liveData)
        return liveData
    }

    private fun fetchUserViewedItems(user: FirebaseUser, liveData: MutableLiveData<List<Item>>) {
        val userViewedItemsRef = firebaseInstance.reference.child("users").child(user.uid).child("viewedItems")
        userViewedItemsRef.orderByChild("viewedOn").limitToLast(6)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    val itemCount = dataSnapshot.childrenCount
                    if (itemCount == 0L) {
                        liveData.value = items
                    } else {
                        processUserViewedItems(dataSnapshot, items, liveData)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve viewed items", databaseError.toException())
                }
            })
    }

    private fun processUserViewedItems(dataSnapshot: DataSnapshot, items: MutableList<Item>, liveData: MutableLiveData<List<Item>>) {
        var processedItems = 0L
        val itemCount = dataSnapshot.childrenCount
        for (itemSnapshot in dataSnapshot.children) {
            val itemId = itemSnapshot.key
            if (itemId != null) {
                fetchItemForUserViewedItems(itemId, items, itemCount, processedItems, liveData)
            } else {
                processedItems++
                if (processedItems == itemCount) {
                    liveData.value = items
                }
            }
        }
    }

    private fun fetchItemForUserViewedItems(itemId: String, items: MutableList<Item>, itemCount: Long, processedItems: Long, liveData: MutableLiveData<List<Item>>) {
        firebaseInstance.reference.child("items").child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = getItemFromSnapshot(dataSnapshot)
                    item?.let { items.add(it) }
                    if (processedItems + 1 == itemCount) {
                        liveData.value = items
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve viewed items", databaseError.toException())
                }
            })
    }
    //endregion
}