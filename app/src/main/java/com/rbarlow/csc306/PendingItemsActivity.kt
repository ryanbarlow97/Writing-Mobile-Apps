package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PendingItemsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search)

        recyclerView = findViewById(R.id.search_results_recycler_view)
        adapter = ItemsAdapter(emptyList(), true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : ItemsAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                val intent = Intent(this@PendingItemsActivity, ItemDetailsActivity::class.java)
                intent.putExtra("id", item.id)
                startActivity(intent)
            }
        })

        firebaseRepository.getAllUnapprovedItems().observe(this) { items ->
            adapter.updateItems(items)
        }
    }
}