package com.omarinc.shopify.home.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ActivityMainBinding
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //setSupportActionBar(findViewById(R.id.toolbar))


        navController =
            Navigation.findNavController(this, R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)

        NavigationUI.setupWithNavController(binding.bottomNav, navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}