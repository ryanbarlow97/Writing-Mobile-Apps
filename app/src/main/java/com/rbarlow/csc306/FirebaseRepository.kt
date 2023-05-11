package com.rbarlow.csc306

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*

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
                        val name = itemSnapshot.child("name").getValue(String::class.java)
                        val description = itemSnapshot.child("description").getValue(String::class.java)
                        val image = itemSnapshot.child("image").getValue(String::class.java)

                        if (name != null && description != null && image != null) {
                            val item = Item(name, description, image, addedBy = "admin", addedOn = "today")
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

}