package com.rbarlow.csc306

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.util.UUID

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemDescriptionEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var itemsReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var filePath: Uri? = null

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            handleImageSelectionResult(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        initViews()
        initFirebaseReferences()


    }

    private fun initViews() {
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemDescriptionEditText = findViewById(R.id.itemDescriptionEditText)
        imageView = findViewById(R.id.imageView)
        val addButton: Button = findViewById(R.id.addButton)
        val chooseButton: Button = findViewById(R.id.chooseButton)

        chooseButton.setOnClickListener { chooseImage() }
        addButton.setOnClickListener { addItem() }
    }

    private fun initFirebaseReferences() {
        itemsReference = FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app").reference.child("items")
        storageReference = FirebaseStorage.getInstance("gs://csc306b.appspot.com").reference
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun handleImageSelectionResult(data: Intent?) {
        if (data != null && data.data != null) {
            filePath = data.data
            try {
                Glide.with(this)
                    .load(filePath)
                    .override(1024) // resize the image
                    .centerCrop() // or fitCenter()
                    .into(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addItem() {
        val name = itemNameEditText.text.toString()
        val description = itemDescriptionEditText.text.toString()

        if (name.isEmpty() || description.isEmpty() || filePath == null) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
        } else {
            uploadImageAndCreateNewItem(name, description)
        }
    }

    private fun uploadImageAndCreateNewItem(name: String, description: String) {
        val alertDialog = createUploadingDialog()

        alertDialog.show()

        val progressBar: ProgressBar = alertDialog.findViewById(R.id.uploadingProgressBar)!!
        val percentageTextView: TextView = alertDialog.findViewById(R.id.uploadingPercentageTextView)!!

        val ref = storageReference.child("images/" + UUID.randomUUID().toString())

        val uploadTask = ref.putFile(filePath!!)

        val progressListener = OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressBar.progress = progress
            percentageTextView.text = getString(R.string.uploading_percentage, progress)
        }

        uploadTask.addOnProgressListener(progressListener)

        uploadTask.addOnSuccessListener {
            alertDialog.dismiss()
            Toast.makeText(this@AddItemActivity, "Uploaded", Toast.LENGTH_SHORT).show()
            ref.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                createNewItem(this, name, description, imageUrl)
            }
        }
            .addOnFailureListener { e ->
                alertDialog.dismiss()
                Toast.makeText(
                    this@AddItemActivity,
                    "Failed " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun createUploadingDialog(): AlertDialog {
        val alertDialog = AlertDialog.Builder(this).create()
        val view = LayoutInflater.from(this).inflate(R.layout.uploading_dialog_layout, null)
        alertDialog.setView(view)
        alertDialog.setCancelable(false)
        return alertDialog
    }

    private fun createNewItem(context: Context, name: String, description: String, imageUrl: String) {
        val itemKey = itemsReference.push().key // this will create a unique key for the new item

        val user = FirebaseAuth.getInstance().currentUser
        //use firebase repository to get user role
        if (user != null) {
            val userRoleRef = FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(user.uid).child("role")
            userRoleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val role = dataSnapshot.value.toString()
                    val isCurator = role == "curator"

                    if (itemKey != null) {
                        val item = mapOf(
                            "name" to name,
                            "description" to description,
                            "image" to imageUrl,
                            "addedBy" to (FirebaseAuth.getInstance().currentUser?.email ?: ""),"addedOn" to System.currentTimeMillis(),
                            "views" to 0,
                            "approved" to isCurator,
                        )
                        itemsReference.child(itemKey).setValue(item).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Item added successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}