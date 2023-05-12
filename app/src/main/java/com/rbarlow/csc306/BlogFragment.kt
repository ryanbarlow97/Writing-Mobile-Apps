package com.rbarlow.csc306

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rbarlow.csc306.databinding.FragmentBlogBinding


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
        val view = inflater.inflate(R.layout.fragment_blog, container, false)

        val blogRecyclerView: RecyclerView = view.findViewById(R.id.blog_recycler_view)
        blogRecyclerView.layoutManager = LinearLayoutManager(context)
        //list of images (image)
        val items = listOf(
            Item("sss", "Bookmark 1", "This is the description for Bookmark 1.", "https://firebasestorage.googleapis.com/v0/b/csc306b.appspot.com/o/images%2F9062ddfa-7c71-49e1-be80-90f16c0e7e41?alt=media&token=76441f01-8662-442e-9bf7-de405226acd2", System.currentTimeMillis(), "SysAdmin", 0)
        )



        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}