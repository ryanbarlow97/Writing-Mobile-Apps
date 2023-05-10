package com.rbarlow.csc306

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rbarlow.csc306.databinding.FragmentBlogBinding
import com.rbarlow.csc306.databinding.FragmentBookmarkBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BlogFragment : Fragment() {

    private var _binding: FragmentBlogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val blogRecyclerView: RecyclerView = view.findViewById(R.id.bookmark_recycler_view)
        blogRecyclerView.layoutManager = LinearLayoutManager(context)

        val items = listOf(
            Item("Bookmark 1", "This is the description for Bookmark 1.", R.drawable.ibm5151),
            Item("Bookmark 2", "This is the description for Bookmark 2.", R.drawable.ibm5151)
        )

        val adapter = BookmarkAdapter(items)
        blogRecyclerView.adapter = adapter

        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}