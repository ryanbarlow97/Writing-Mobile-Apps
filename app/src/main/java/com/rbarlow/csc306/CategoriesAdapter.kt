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

class CategoriesAdapter(var categories: List<Category>, private val lifecycleOwner: LifecycleOwner, private val context: Context)
    : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

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
            } else if (category.title == "Viewed") {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    FirebaseRepository().getUserViewedItems(currentUser).observe(lifecycleOwner) { viewedItems ->
                        //show in reverse order
                        itemsAdapter.updateItems(viewedItems.reversed())
                    }
                } else {
                    //remove the Viewed category if the user is not logged in
                    categories = categories.filter { it.title != "Viewed" }
                }
            }

            itemsAdapter.setOnItemClickListener(object : ItemsAdapter.OnItemClickListener {
                override fun onItemClick(item: Item) {
                    val intent = Intent(context, ItemDetailsActivity::class.java)
                    intent.putExtra("id", item.id)
                    context.startActivity(intent)
                }
            })
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}