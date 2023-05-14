package com.rbarlow.csc306

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateBlogPostActivity : AppCompatActivity() {

    private lateinit var postTitleEditText: EditText
    private lateinit var postContentEditText: EditText
    private lateinit var savePostButton: Button
    private lateinit var postReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_blog_post)

        postTitleEditText = findViewById(R.id.post_title_edit_text)
        postContentEditText = findViewById(R.id.post_content_edit_text)
        savePostButton = findViewById(R.id.save_post_button)

        savePostButton.setOnClickListener {
            println("Save post button clicked")
            saveNewPost()
        }

        initFirebaseReferences()
    }

    private fun initFirebaseReferences() {
        postReference = FirebaseDatabase.getInstance("https://csc306b-default-rtdb.europe-west1.firebasedatabase.app").reference.child("blogPosts")
    }

    private fun saveNewPost() {
        val title = postTitleEditText.text.toString()
        val content = postContentEditText.text.toString()
        val author = FirebaseAuth.getInstance().currentUser?.email.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            createNewBlogPost(title, content, author)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewBlogPost(title: String, content: String, author: String) {
        val postKey = postReference.push().key

        if (postKey != null) {
            val blogPost = mapOf(
                "title" to title,
                "content" to content,
                "author" to author,
                "addedOn" to System.currentTimeMillis()
            )
            postReference.child(postKey).setValue(blogPost).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Blog post added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}