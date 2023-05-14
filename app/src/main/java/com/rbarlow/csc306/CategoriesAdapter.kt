package com.rbarlow.csc306

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class CategoriesAdapter(
    var categories: List<Category>,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_layout, parent, false)
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
        private lateinit var itemsAdapter: ItemsAdapter

        fun bind(category: Category, listener: OnItemClickListener?) {
            setCategoryName(category)
            setUpItemsRecyclerView(listener)

            when (category.title) {
                "New" -> populateNewItems()
                "Top" -> populateHotItems()
                "Viewed" -> populateViewedItems()
            }
        }

        private fun setCategoryName(category: Category) {
            categoryNameTextView.text = category.title
        }

        private fun setUpItemsRecyclerView(listener: OnItemClickListener?) {
            itemsAdapter = ItemsAdapter(emptyList(), false)
            itemsAdapter.setOnItemClickListener(object : ItemsAdapter.OnItemClickListener {
                override fun onItemClick(item: Item) {
                    listener?.onItemClick(item)
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    intent.putExtra("id", item.id)
                    context.startActivity(intent)
                }
            })
            itemsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemsRecyclerView.adapter = itemsAdapter
        }


        private fun populateNewItems() {
            FirebaseRepository().getNewestItems().observe(lifecycleOwner) { newestItems ->
                itemsAdapter.updateItems(newestItems)
            }
        }

        private fun populateHotItems() {
            FirebaseRepository().getMostViewedItems().observe(lifecycleOwner) { hotItems ->
                itemsAdapter.updateItems(hotItems)
            }
        }

        private fun populateViewedItems() {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                FirebaseRepository().getUserViewedItems(currentUser).observe(lifecycleOwner) { viewedItems ->
                    itemsAdapter.updateItems(viewedItems.reversed())
                }
            } else {
                removeViewedCategory()
            }
        }

        private fun removeViewedCategory() {
            categories = categories.filter { it.title != "Viewed" }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}