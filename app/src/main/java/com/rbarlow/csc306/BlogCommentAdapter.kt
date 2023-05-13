package com.rbarlow.csc306

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class BlogCommentAdapter(var comments: List<BlogComment>) : RecyclerView.Adapter<BlogCommentAdapter.BlogCommentViewHolder>() {

    class BlogCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.comment_author)
        val content: TextView = itemView.findViewById(R.id.comment_text)
        val timestamp: TextView = itemView.findViewById(R.id.comment_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogCommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return BlogCommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogCommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.author.text = comment.author
        holder.content.text = comment.content
        holder.timestamp.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(comment.addedOn))
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}