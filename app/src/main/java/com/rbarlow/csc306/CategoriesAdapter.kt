package com.rbarlow.csc306

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(private val categories: List<Category>) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.category_title)
        private val recyclerView: RecyclerView = view.findViewById(R.id.category_list)

        fun bind(category: Category) {
            titleTextView.text = category.title

            val adapter = ItemsAdapter(category.items)
            recyclerView.layoutManager = LinearLayoutManager(
                recyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.adapter = adapter

            // Set the nested RecyclerView's height based on its content
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            // Set OnClickListener for each item view in the ItemsAdapter
            adapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(item: Item) {
                    val intent = Intent(itemView.context, ItemDetailsActivity::class.java)
                    intent.putExtra("title", item.title)
                    intent.putExtra("description", item.description)
                    intent.putExtra("imageResource", item.imageResourceId)
                    itemView.context.startActivity(intent)
                }
            })
        }
    }

    // Interface to handle click events on items in ItemsAdapter
    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}