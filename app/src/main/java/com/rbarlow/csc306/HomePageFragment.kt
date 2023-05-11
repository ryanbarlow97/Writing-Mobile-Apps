package com.rbarlow.csc306

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rbarlow.csc306.databinding.FragmentHomePageBinding

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var binding: FragmentHomePageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoriesAdapter(getDummyCategory())
        binding.recyclerHomePage.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerHomePage.adapter = adapter
        sharedViewModel.items.value = getDummyItems()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDummyCategory(): List<Category> {
        val trendingItems = getDummyItems()
        val recommendedItems = getDummyItems()
        val continueViewingItems = getDummyItems()
        val viewAgainItems = getDummyItems()


        return listOf(
            Category("Trending", trendingItems),
            Category("Recommended for You", recommendedItems),
            Category("Continue Viewing", continueViewingItems),
            Category("View Again", viewAgainItems),
            )
    }

    private fun getDummyItems(): List<Item> {
        // Return a list of dummy recommended items
        return listOf(
            Item(
                "iMac G3",
                "The iMac G3, originally released as the iMac, is a series of Macintosh personal " +
                        "computers sold by Apple Computer from 1998 to 2003. The iMac was the first major new " +
                        "product release for Apple under Steve Jobs, Apple's CEO and cofounder, who returned to the " +
                        "financially troubled company in 1996 after eleven years away. Jobs reorganized the company " +
                        "and simplified the product line; the iMac was designed as Apple's new consumer desktop " +
                        "product, a cheaper computer for average consumers that would easily connect to the internet.",
                R.mipmap.ic_imacg3_foreground
            ),
            Item(
                "IBM 5151",
                "The iMac G3, originally released as the iMac, is a series of Macintosh personal " +
            "computers sold by Apple Computer from 1998 to 2003. The iMac was the first major new ",
                    R.drawable.ibm5151
            ),
            Item(
                "Polaroid",
                "Item 3 description",
                R.drawable.sun630
            ),
            Item(
                "Leica Standard",
                "Item 4 description",
                R.drawable.leica_standard_1932
            ),
            Item(
                "Item 5",
                "Item 5 description",
                com.google.android.material.R.drawable.ic_keyboard_black_24dp
            ),
            Item(
                "Item 6",
                "Item 6 description",
                com.google.android.material.R.drawable.abc_btn_radio_material
            ),
            Item(
                "Item 7",
                "Item 7 description",
                com.google.android.material.R.drawable.abc_btn_check_material
            ),
            Item(
                "Item 8",
                "Item 8 description",
                com.google.android.material.R.drawable.material_ic_calendar_black_24dp
            ),
        ).shuffled()
    }
}