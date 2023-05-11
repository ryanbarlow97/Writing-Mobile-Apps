package com.rbarlow.csc306

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rbarlow.csc306.databinding.FragmentBookmarkBinding
import java.time.Instant

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val bookmarkRecyclerView: RecyclerView = view.findViewById(R.id.bookmark_recycler_view)
        bookmarkRecyclerView.layoutManager = LinearLayoutManager(context)

        val items = listOf(
            Item("Bookmark 1", "This is the description for Bookmark 1.", "https://firebasestorage.googleapis.com/v0/b/csc306b.appspot.com/o/images%2F9062ddfa-7c71-49e1-be80-90f16c0e7e41?alt=media&token=76441f01-8662-442e-9bf7-de405226acd2", Instant.now().toString(), "SysAdmin")
        )

        val adapter = BookmarkAdapter(items)
        bookmarkRecyclerView.adapter = adapter

        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}