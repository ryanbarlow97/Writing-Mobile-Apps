package com.rbarlow.csc306

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.*

class EditItemActivity : AppCompatActivity() {

    private lateinit var editButton: MaterialButton
    private lateinit var deleteButton: MaterialButton
    private lateinit var chooseButton: Button
    private lateinit var imageView: ImageView
    private lateinit var itemNameEditText: TextInputEditText
    private lateinit var itemDescriptionEditText: TextInputEditText

    private val itemsReference = FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app").reference.child("items")
    private val storageReference = FirebaseStorage.getInstance("gs://csc306b.appspot.com").reference

    private var itemKey: String? = null
    private var imageUrl: String? = null
    private var filePath: Uri? = null

    var firebaseRepository = FirebaseRepository()

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
        setContentView(R.layout.activity_edit_item)

        // Initialize views
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)
        chooseButton = findViewById(R.id.chooseButton)
        imageView = findViewById(R.id.imageView)
        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemDescriptionEditText = findViewById(R.id.itemDescriptionEditText)

        //get the item info from the repository
        itemKey = intent.getStringExtra("id")
        firebaseRepository.getItem(itemKey.toString()).observe(this) { item: Item ->
            itemNameEditText.setText(item.name)
            itemDescriptionEditText.setText(item.description)
            imageUrl = item.image
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(imageView)
            }
        }

        // Set OnClickListener for the edit button
        editButton.setOnClickListener {
            val name = itemNameEditText.text.toString()
            val description = itemDescriptionEditText.text.toString()

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill in all fields and select an image",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (filePath != null) {
                    val ref = storageReference.child("images/" + UUID.randomUUID().toString())

                    ref.putFile(filePath!!)
                        .addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                val newImageUrl = uri.toString()
                                updateItem(name, description, newImageUrl)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@EditItemActivity,
                                "Failed " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    updateItem(name, description, imageUrl!!)
                }
            }
        }

        // Set OnClickListener for the delete button
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Set OnClickListener for the choose button
        chooseButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startForResult.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }

    private fun updateItem(name: String, description: String, imageUrl: String) {
        if (itemKey != null) {
            val itemUpdates = mapOf(
                "name" to name,
                "description" to description,
                "image" to imageUrl,
                "addedOn" to System.currentTimeMillis(),
                "approved" to true
            )

            itemsReference.child(itemKey!!).updateChildren(itemUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK) // Add this line to set the result
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete item")
        builder.setMessage("Are you sure you want to delete this item?")

        builder.setPositiveButton("Yes") { _, _ ->
            if (itemKey != null) {
                firebaseRepository.deleteItemWithReferences(itemKey!!) { success, message ->
                    if (success) {
                        showToast("Item deleted successfully")
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showToast("Error: $message")
                    }
                }
            }
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}