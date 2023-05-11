package com.rbarlow.csc306

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels

class SearchFragment : Fragment() {

    private lateinit var items: List<Item>
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items = sharedViewModel.items.value ?: emptyList()

        // Initialize the RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.search_results_recycler_view)
        adapter = ItemsAdapter(items, true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the search input and add a listener for text changes
        searchInput = view.findViewById(R.id.search_input)
        searchInput.addTextChangedListener {
            searchItems(it.toString())
        }
    }


    private fun searchItems(query: String) {
        val filteredItems = items.filter { item ->
            item.title.contains(query, ignoreCase = true)
        }
        adapter.updateItems(filteredItems.toMutableList())
    }

}
