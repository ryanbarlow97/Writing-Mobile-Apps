package com.rbarlow.csc306

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
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
    }

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
            startForResult.launch(Intent.createChooser(intent, "Select Picture"))
        }

        addButton.setOnClickListener {
            val name = itemNameEditText.text.toString()
            val description = itemDescriptionEditText.text.toString()

            if (name.isEmpty() || description.isEmpty() || filePath == null) {
                Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            } else {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Uploading...")
                    .setCancelable(false)
                    .create()

                alertDialog.show()

                val ref = storageReference.child("images/" + UUID.randomUUID().toString())

                ref.putFile(filePath!!)
                    .addOnSuccessListener {
                        alertDialog.dismiss()
                        Toast.makeText(this@AddItemActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            createNewItem(name, description, imageUrl)
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
        }
    }

    private fun createNewItem(name: String, description: String, imageUrl: String) {
        val itemKey = itemsReference.push().key // this will create a unique key for the new item

        if (itemKey != null) {
            val item = mapOf(
                "name" to name,
                "description" to description,
                "image" to imageUrl,
                "addedBy" to (FirebaseAuth.getInstance().currentUser?.email ?: ""),
                "addedOn" to System.currentTimeMillis(),
                "views" to 0,
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