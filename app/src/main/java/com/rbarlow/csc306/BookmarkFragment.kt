package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rbarlow.csc306.databinding.FragmentBookmarkBinding

class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        val bookmarkRecyclerView: RecyclerView = binding.bookmarkRecyclerView
        bookmarkRecyclerView.layoutManager = LinearLayoutManager(context)

        val progressBar: ProgressBar = binding.bookmarkProgressBar
        val emptyView: TextView = binding.bookmarkEmptyView
        progressBar.visibility = View.VISIBLE

        firebaseRepository = FirebaseRepository()
        currentUser = FirebaseAuth.getInstance().currentUser!!

        bookmarkRecyclerView.adapter = BookmarkAdapter(emptyList())

        firebaseRepository.getBookmarkedItems(currentUser).observe(viewLifecycleOwner) { items ->
            progressBar.visibility = View.GONE
            if (items.isEmpty()) {
                emptyView.visibility = View.VISIBLE
            } else {
                emptyView.visibility = View.GONE
            }

            val adapter = BookmarkAdapter(items)
            bookmarkRecyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : BookmarkAdapter.OnItemClickListener {
                override fun onItemClick(item: Item) {
                    val intent = Intent(requireContext(), ItemDetailsActivity::class.java)
                    intent.putExtra("id", item.id)
                    startActivity(intent)
                }
            })
            bookmarkRecyclerView.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}