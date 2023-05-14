package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth


class BlogFragment : Fragment(), BlogPostAdapter.OnBlogPostClickListener {

    private lateinit var blogPostsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val firebaseRepository = FirebaseRepository()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_blog, container, false)
        progressBar = view.findViewById(R.id.blog_progress_bar)
        progressBar.visibility = View.VISIBLE

        blogPostsRecyclerView = view.findViewById(R.id.blog_posts_recycler_view)
        fab = view.findViewById(R.id.fab)

        blogPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        blogPostsRecyclerView.adapter = BlogPostAdapter(emptyList(), this)


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            firebaseRepository.getUserRole(user.uid).observe(viewLifecycleOwner) { role ->
                if (role == "curator") {
                    fab.visibility = View.VISIBLE
                } else {
                    fab.visibility = View.GONE
                }
            }
        } else {
            fab.visibility = View.GONE
        }

        // Load blog posts and display in RecyclerView
        loadBlogPosts()

        // Set up FAB click listener for creating new blog posts
        fab.setOnClickListener {
            val intent = Intent(requireContext(), CreateBlogPostActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadBlogPosts() {
        // Fetch blog posts from the database
        firebaseRepository.getBlogPosts().observe(viewLifecycleOwner) { blogPosts ->
            progressBar.visibility = View.GONE
            if (blogPosts != null) {
                // Create an instance of BlogPostAdapter and set it to the RecyclerView
                val adapter = BlogPostAdapter(blogPosts, this)
                blogPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                blogPostsRecyclerView.adapter = adapter
            }
        }
    }

    override fun onBlogPostClick(blogPost: BlogPost) {
        // Create an Intent to start the BlogPostDetailActivity
        val intent = Intent(requireContext(), BlogPostDetailActivity::class.java)

        // Pass the blog post data to the BlogPostDetailActivity
        intent.putExtra("blogPostId", blogPost.id)
        intent.putExtra("blogPostTitle", blogPost.title)
        intent.putExtra("blogPostContent", blogPost.content)
        intent.putExtra("blogPostAuthor", blogPost.author)
        intent.putExtra("blogPostTimestamp", blogPost.addedOn)

        // Start the BlogPostDetailActivity
        startActivity(intent)
    }
}