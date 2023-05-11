package com.rbarlow.csc306

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DynamicColors.applyIfAvailable(this)
        setContentView(R.layout.activity_main)


        bottomNavigationView = findViewById(R.id.navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.HomePageFragment)

                    true
                }
                R.id.navigation_exhibition_map -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.SecondFragment)

                    true
                }
                R.id.navigation_search -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.SearchFragment)

                    true
                }
                R.id.navigation_blog -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.BlogFragment)

                    true
                }
                R.id.navigation_bookmarks -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.BookmarksFragment)

                    true
                }
                else -> false
            }
        }
    }
}

