package com.rbarlow.csc306

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(var categories: List<Category>, private val lifecycleOwner: LifecycleOwner)
    : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_layout, parent, false)
        context = parent.context
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, listener)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val categoryNameTextView: TextView = view.findViewById(R.id.category_title)
        private val itemsRecyclerView: RecyclerView = view.findViewById(R.id.category_list)

        fun bind(category: Category, listener: OnItemClickListener?) {
            categoryNameTextView.text = category.title

            val itemsAdapter = ItemsAdapter(emptyList(), false)
            itemsAdapter.setOnItemClickListener(object : ItemsAdapter.OnItemClickListener {
                override fun onItemClick(item: Item) {
                    listener?.onItemClick(item)
                }
            })

            itemsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemsRecyclerView.adapter = itemsAdapter

            // Populate the items based on the category
            if (category.title == "New") {
                FirebaseRepository().getNewestItems().observe(lifecycleOwner) { newestItems ->
                    itemsAdapter.updateItems(newestItems)
                }
            } else if (category.title == "Hot") {
                FirebaseRepository().getMostViewedItems().observe(lifecycleOwner) { hotItems ->
                    itemsAdapter.updateItems(hotItems)
                }
            }
        }
    }






    // Function to set the OnItemClickListener for the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}