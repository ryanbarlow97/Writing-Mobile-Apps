package com.rbarlow.csc306

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class BlogPostDetailActivity : AppCompatActivity() {

    private lateinit var blogPostTitle: TextView
    private lateinit var blogPostTimestamp: TextView
    private lateinit var blogPostContent: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var addCommentEditText: EditText
    private lateinit var addCommentButton: Button
    private lateinit var addCommentBox: TextInputLayout

    private lateinit var commentsAdapter: BlogCommentAdapter
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_post_detail)

        blogPostTitle = findViewById(R.id.blog_post_title)
        blogPostTimestamp = findViewById(R.id.blog_post_timestamp)
        blogPostContent = findViewById(R.id.blog_post_content)
        commentsRecyclerView = findViewById(R.id.comments_recycler_view)
        addCommentEditText = findViewById(R.id.add_comment_edit_text)
        addCommentButton = findViewById(R.id.add_comment_button)
        addCommentBox = findViewById(R.id.add_comment_text_input_layout)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsAdapter = BlogCommentAdapter(emptyList())
        commentsRecyclerView.adapter = commentsAdapter

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null){
            addCommentButton.visibility = View.GONE
            addCommentEditText.visibility = View.GONE
            addCommentBox.visibility = View.GONE
        }

        // Load blog post details and comments
        loadBlogPostDetails()
        loadComments()

        // Set up add comment button click listener
        addCommentButton.setOnClickListener {

            if (addCommentEditText.text.isEmpty()) {
                // Comment is empty, show an error message
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val commentContent = addCommentEditText.text.toString()
            val commentAuthor = FirebaseAuth.getInstance().currentUser?.email.toString()
            val blogPostId = intent.getStringExtra("blogPostId").toString()

            firebaseRepository.addCommentToBlogPost(blogPostId, commentContent, commentAuthor) { success, errorMessage ->
                if (success) {
                    // Comment was added successfully
                    addCommentEditText.text.clear()
                    loadComments()
                } else {
                    // There was an error while adding the comment
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadBlogPostDetails() {
        blogPostTitle.text = intent.getStringExtra("blogPostTitle")
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        blogPostTimestamp.text = getString(R.string.upload_info, intent.getStringExtra("blogPostAuthor"), formatter.format(intent.getLongExtra("blogPostTimestamp", 0L)))
        blogPostContent.text = intent.getStringExtra("blogPostContent")
    }

    private fun loadComments() {
        val blogPostId = intent.getStringExtra("blogPostId").toString()
        firebaseRepository.getBlogPostComments(blogPostId).observe(this) { comments ->
            commentsAdapter.comments = comments
            commentsAdapter.notifyDataSetChanged()
        }
    }
}