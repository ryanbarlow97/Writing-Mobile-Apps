package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rbarlow.csc306.databinding.FragmentHomePageBinding

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private val firebaseRepository = FirebaseRepository()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        progressBar = binding.homePageProgressBar
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar.visibility = View.VISIBLE
        setupRecyclerView()
        observeCategories()
        observeUserRole()

    }

    private fun setupRecyclerView() {
        val adapter = CategoriesAdapter(emptyList(), viewLifecycleOwner, requireContext())
        binding.recyclerHomePage.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHomePage.adapter = adapter
    }

    private fun observeCategories() {
        firebaseRepository.getCategories().observe(viewLifecycleOwner) { categories ->
            progressBar.visibility = View.GONE
            if (categories != null) {
                val adapter = binding.recyclerHomePage.adapter as CategoriesAdapter
                adapter.categories = categories
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeUserRole() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.addItemButton.visibility = View.VISIBLE
            binding.addItemButton.setOnClickListener {
                val intent = Intent(requireActivity(), AddItemActivity::class.java)
                startActivity(intent)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        observeCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}