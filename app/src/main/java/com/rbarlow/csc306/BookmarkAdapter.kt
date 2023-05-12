package com.rbarlow.csc306

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class BookmarkAdapter(private val dataSet: List<Item>) :
    RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.bookmark_title)
        val descriptionTextView: TextView = view.findViewById(R.id.bookmark_description)
        val timestampTextView: TextView = view.findViewById(R.id.bookmark_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTextView.text = dataSet[position].name
        holder.descriptionTextView.text = dataSet[position].description

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val timestamp = formatter.format(dataSet[position].addedOn)
        holder.timestampTextView.text = "Bookmarked on: $timestamp"
    }

    override fun getItemCount() = dataSet.size

}