package com.rbarlow.csc306

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BlogPostAdapter(private val blogPosts: List<BlogPost>, private val onBlogPostClickListener: OnBlogPostClickListener) : RecyclerView.Adapter<BlogPostAdapter.BlogPostViewHolder>() {

    // This interface will be used to handle clicks on blog post items in the RecyclerView
    interface OnBlogPostClickListener {
        fun onBlogPostClick(blogPost: BlogPost)
    }

    // ViewHolder class for holding references to the views in the item_blog_post XML layout
    class BlogPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.blog_post_title)
        val author: TextView = itemView.findViewById(R.id.blog_post_author)
        val timestamp: TextView = itemView.findViewById(R.id.blog_post_timestamp)
    }

    // This method inflates the item_blog_post XML layout and returns a ViewHolder instance with the views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_post, parent, false)
        return BlogPostViewHolder(view)
    }

    // This method binds the data from the BlogPost object to the views in the ViewHolder
    override fun onBindViewHolder(holder: BlogPostViewHolder, position: Int) {
        val blogPost = blogPosts[position]

        holder.title.text = blogPost.title
        holder.author.text = blogPost.author
        holder.timestamp.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(blogPost.addedOn))

        // Set an onClickListener on the itemView to handle clicks on blog post items
        holder.itemView.setOnClickListener {
            onBlogPostClickListener.onBlogPostClick(blogPost)
        }
    }

    // This method returns the number of items in the blogPosts list
    override fun getItemCount(): Int {
        return blogPosts.size
    }
}