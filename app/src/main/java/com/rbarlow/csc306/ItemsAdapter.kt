package com.rbarlow.csc306

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ItemsAdapter(private var items: MutableList<Item>) :
    RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {


    private lateinit var context: Context
    private lateinit var listener: CategoriesAdapter.OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, listener)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.item_title)
        private val imageView: ImageView = view.findViewById(R.id.featured_item_image)

        fun bind(item: Item, listener: CategoriesAdapter.OnItemClickListener) {
            titleTextView.text = item.title
            imageView.setImageResource(item.imageResourceId)

            // Set OnClickListener for the item view
            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    // Function to set the OnItemClickListener for the adapter
    fun setOnItemClickListener(listener: CategoriesAdapter.OnItemClickListener) {
        this.listener = listener
    }

    fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}