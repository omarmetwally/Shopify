package com.omarinc.shopify.home.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.omarinc.shopify.AuthenticationMainActivity
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ActivityMainBinding
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Helper

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: ISharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferencesImpl.getInstance(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)


        navController =
            Navigation.findNavController(this, R.id.nav_host_fragment)
//        setupActionBarWithNavController(navController)
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        NavigationUI.setupWithNavController(binding.bottomNav, navController)
        // 3lshan el bottomNavigation y5tafe
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id in Constants.fragmentsWithHiddenBottomNav) {
//                binding.bottomNav.visibility = View.VISIBLE
//            } else {
//                binding.bottomNav.visibility = View.GONE
//            }
//        }
        navigationItemSelectedListener()

    }

    private fun navigationItemSelectedListener() {
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profileFragment -> {
                    if (checkUserTokenExist() == "null") {
                        showGuestModeAlertDialog()
                        false
                    } else {
                        navController.navigate(R.id.profileFragment)
                        true
                    }

                }

                R.id.shoppingCartFragment -> {
                    if (checkUserTokenExist() == "null") {
                        showGuestModeAlertDialog()
                        false
                    } else {
                        navController.navigate(R.id.shoppingCartFragment)
                        true
                    }
                }

                R.id.categoriesFragment -> {

                    navController.navigate(R.id.categoriesFragment)
                    true
                }
                R.id.homeFragment -> {

                    navController.navigate(R.id.homeFragment)
                    true
                }

                else -> false
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


    private fun checkUserTokenExist(): String {
        return sharedPreferences.readStringFromSharedPreferences(Constants.USER_TOKEN)
    }

    private fun showGuestModeAlertDialog() {
        Helper.showAlertDialog(
            context = this,
            title = getString(R.string.guest_mode),
            message = getString(R.string.guest_mode_message),
            positiveButtonText = getString(R.string.login),
            positiveButtonAction = {
                invertSkippedFlag()
                navigateToLogin()
            },
            negativeButtonText = getString(R.string.no)
        )
    }

    private fun invertSkippedFlag() {
        sharedPreferences.writeBooleanToSharedPreferences(Constants.USER_SKIPPED, false)
    }

    private fun navigateToLogin() {

        startActivity(Intent(applicationContext, AuthenticationMainActivity::class.java))
        finish()

    }
}