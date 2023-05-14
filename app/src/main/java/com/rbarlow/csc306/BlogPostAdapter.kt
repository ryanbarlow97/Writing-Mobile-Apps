package com.rbarlow.csc306

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BlogPostAdapter(private val blogPosts: List<BlogPost>, private val onBlogPostClickListener: OnBlogPostClickListener) : RecyclerView.Adapter<BlogPostAdapter.BlogPostViewHolder>() {

    interface OnBlogPostClickListener {
        fun onBlogPostClick(blogPost: BlogPost)
    }

    class BlogPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.blog_post_title)
        val author: TextView = itemView.findViewById(R.id.blog_post_author)
        val timestamp: TextView = itemView.findViewById(R.id.blog_post_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog_post, parent, false)
        return BlogPostViewHolder(view)
    }

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

    override fun getItemCount(): Int {
        return blogPosts.size
    }
}