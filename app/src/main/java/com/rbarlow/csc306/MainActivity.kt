package com.rbarlow.csc306

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DynamicColors.applyIfAvailable(this)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val userNameTextView = toolbar.findViewById<TextView>(R.id.toolbar_email)

        if (FirebaseAuth.getInstance().currentUser == null) {
            userNameTextView.text = "Not logged in"
        } else {
            userNameTextView.text = FirebaseAuth.getInstance().currentUser?.email
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)
        // Add drawer toggle to the toolbar
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        updateMenuItems()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login -> {
                    // Navigate to the LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.logout -> {
                    // Sign out from Firebase and navigate to the LoginActivity
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    updateMenuItems()
                    true
                }
                else -> false
            }
        }



        bottomNavigationView = findViewById(R.id.navigation)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val menuNav = bottomNavigationView.menu
            menuNav.findItem(R.id.navigation_bookmarks).isVisible = false
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.HomePageFragment)

                    true
                }
                R.id.navigation_search -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.SearchFragment)

                    true
                }
                R.id.navigation_blog -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.BlogFragment)

                    true
                }
                R.id.navigation_bookmarks -> {

                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                    val navController = navHostFragment.findNavController()
                    navController.navigate(R.id.BookmarksFragment)

                    true
                }
                else -> false
            }
        }
    }

    private fun updateMenuItems() {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val logoutItem = navigationView.menu.findItem(R.id.logout)
        val loginItem = navigationView.menu.findItem(R.id.login)
        logoutItem.isVisible = isLoggedIn
        loginItem.isVisible = !isLoggedIn
    }
}

