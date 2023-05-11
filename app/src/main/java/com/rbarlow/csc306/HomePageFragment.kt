package com.rbarlow.csc306

import com.rbarlow.csc306.FirebaseRepository
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rbarlow.csc306.databinding.FragmentHomePageBinding

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private val firebaseRepository = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userNameTextView.text = FirebaseAuth.getInstance().currentUser?.email

        // Initialize the RecyclerView with an empty adapter
        val adapter = CategoriesAdapter(emptyList())
        binding.recyclerHomePage.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHomePage.adapter = adapter

        // Get the categories from the database and update the adapter
        firebaseRepository.getCategories(FirebaseAuth.getInstance().currentUser?.uid.toString()).observe(viewLifecycleOwner, Observer { categories ->
            // Update the adapter or perform any other necessary actions with the categories
            if (categories != null) {
                adapter.categories = categories
                adapter.notifyDataSetChanged()
            }
        })


        //show curator stuff (button and image) if the user is a curator
        firebaseRepository.getUserRole(FirebaseAuth.getInstance().currentUser?.uid.toString()).observe(viewLifecycleOwner, Observer { userRole ->
            if (userRole == "curator") {
                binding.imageView.visibility = View.VISIBLE
                binding.addItemButton.visibility = View.VISIBLE
                binding.addItemButton.setOnClickListener {
                    val intent = Intent(requireActivity(), AddItemActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
