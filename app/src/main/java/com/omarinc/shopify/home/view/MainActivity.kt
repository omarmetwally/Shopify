package com.omarinc.shopify.home.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ActivityMainBinding
import com.omarinc.shopify.utilities.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)



        navController =
            Navigation.findNavController(this, R.id.nav_host_fragment)
//        setupActionBarWithNavController(navController)
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        NavigationUI.setupWithNavController(binding.bottomNav, navController)
        // 3lshan el bottomNavigation y5tafe
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in Constants.fragmentsWithHiddenBottomNav) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }
    }

  /*  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.testFragment -> {
                navController.navigate(R.id.testFragment)
                true
            }
            R.id.testFragment -> {
                navController.navigate(R.id.testFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
*/
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}