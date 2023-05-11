package com.rbarlow.csc306

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ItemsAdapter(items: List<Item>, private val isWideLayout: Boolean) :
    RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private val mutableItems: MutableList<Item> = items.toMutableList()
    private lateinit var context: Context
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId = if (isWideLayout) R.layout.item_layout_wide else R.layout.item_layout
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mutableItems[position]
        holder.bind(item, listener)
    }

    override fun getItemCount(): Int = mutableItems.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.item_title)
        private val imageView: ImageView = view.findViewById(R.id.featured_item_image)

        fun bind(item: Item, listener: OnItemClickListener?) {
            titleTextView.text = item.name

            // Load image from URL into imageView
            item.image?.let { url ->
                Glide.with(imageView.context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for performance
                    .into(imageView)
            }

            // Set OnClickListener for the item view
            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
    }

    // Function to set the OnItemClickListener for the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateItems(newItems: List<Item>) {
        mutableItems.clear()
        mutableItems.addAll(newItems)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }

}
