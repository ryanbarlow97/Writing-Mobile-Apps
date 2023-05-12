package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener

class SearchFragment : Fragment() {

    private var allItems: List<Item> = emptyList() // Store all items
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private val firebaseRepository = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.search_results_recycler_view)
        adapter = ItemsAdapter(emptyList(), true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.setOnItemClickListener(object : ItemsAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                val intent = Intent(requireContext(), ItemDetailsActivity::class.java)
                intent.putExtra("itemName", item.name)
                startActivity(intent)
            }
        })


        searchInput = view.findViewById(R.id.search_input)
        searchInput.addTextChangedListener { text ->
            val searchString = text.toString().trim()
            if (searchString.isNotEmpty()) {
                val filteredItems = filterItems(searchString)
                adapter.updateItems(filteredItems)
            } else {
                adapter.updateItems(allItems)
            }
        }
        firebaseRepository.getAllItems().observe(viewLifecycleOwner) { items ->
            allItems = items
            println("All items: $items")
            adapter.updateItems(items)
        }

    }

    private fun filterItems(searchString: String): List<Item> {
        return allItems.filter { item ->
            item.name.contains(searchString, ignoreCase = true)
        }
    }
}
