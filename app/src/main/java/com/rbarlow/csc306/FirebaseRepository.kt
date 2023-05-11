import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.rbarlow.csc306.Category
import com.rbarlow.csc306.Item

class FirebaseRepository {

    private val firebaseInstance =
        FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app")


    fun getItem(itemId: String): MutableLiveData<Item?> {
        val liveData = MutableLiveData<Item?>()

        firebaseInstance.reference.child("items").child(itemId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val item = dataSnapshot.getValue(Item::class.java)
                    liveData.value = item
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Could not retrieve item", databaseError.toException())
                }
            })

        return liveData
    }

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


}