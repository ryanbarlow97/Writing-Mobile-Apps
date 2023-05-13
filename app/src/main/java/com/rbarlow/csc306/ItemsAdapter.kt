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

class ItemsAdapter(
    private var items: List<Item>,
    private val isWideLayout: Boolean
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId = getLayoutResId()
        val view = inflateView(parent, layoutResId)
        context = parent.context
        return ViewHolder(view)
    }

    private fun getLayoutResId(): Int {
        return if (isWideLayout) R.layout.item_layout_wide else R.layout.item_layout
    }

    private fun inflateView(parent: ViewGroup, layoutResId: Int): View {
        return LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.item_title)
        private val imageView: ImageView = view.findViewById(R.id.featured_item_image)

        fun bind(item: Item) {
            setTitle(item.name)
            loadImage(item.image)
            setOnClickListener(item)
        }

        private fun setTitle(title: String) {
            titleTextView.text = title
        }

        private fun loadImage(url: String) {
            Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image for performance
                .into(imageView)
        }

        private fun setOnClickListener(item: Item) {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(item)
            }
        }
    }

    // Function to set the OnItemClickListener for the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

}