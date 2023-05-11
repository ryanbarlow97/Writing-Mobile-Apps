package com.rbarlow.csc306

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.UUID

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemDescriptionEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var itemsReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemDescriptionEditText = findViewById(R.id.itemDescriptionEditText)
        imageView = findViewById(R.id.imageView)
        val addButton: Button = findViewById(R.id.addButton)
        val chooseButton: Button = findViewById(R.id.chooseButton)

        itemsReference = FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app").reference.child("items")
        storageReference = FirebaseStorage.getInstance("gs://csc306b.appspot.com").reference

        chooseButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        addButton.setOnClickListener {
            val name = itemNameEditText.text.toString()
            val description = itemDescriptionEditText.text.toString()

            if (name.isEmpty() || description.isEmpty() || filePath == null) {
                Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            } else {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                val ref = storageReference.child("images/" + UUID.randomUUID().toString())

                ref.putFile(filePath!!)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this@AddItemActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            createNewItem(name, description, imageUrl)
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@AddItemActivity,
                            "Failed " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun createNewItem(name: String, description: String, imageUrl: String) {
        val itemKey = itemsReference.push().key // this will create a unique key for the new item

        if (itemKey != null) {
            val item = mapOf(
                "name" to name,
                "description" to description,
                "image" to imageUrl
            )

            itemsReference.child(itemKey).setValue(item).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
