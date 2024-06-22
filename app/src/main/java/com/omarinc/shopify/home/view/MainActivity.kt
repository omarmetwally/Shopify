package com.omarinc.shopify.home.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.omarinc.shopify.AuthenticationMainActivity
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ActivityMainBinding
import com.omarinc.shopify.databinding.NoInternetBannerBinding
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Helper

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: ISharedPreferences



    private var wasNetworkDisconnected = false
    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
                showNetworkBanner(noConnectivity)
                if (wasNetworkDisconnected && !noConnectivity) {
                    refreshCurrentFragment()
                    wasNetworkDisconnected = false
                } else {
                    wasNetworkDisconnected = noConnectivity
                }
            }
        }
    }

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

        setupNetworkBanner()
        navigationItemSelectedListener()

    }

    override fun onStart() {
        super.onStart()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)

    }
    private fun setupNetworkBanner() {

        binding.noInternetBanner.buttonTurnOnWifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        binding.noInternetBanner.btnDismiss.setOnClickListener {
            binding.noInternetBanner.networkBanner.visibility = View.GONE
        }
    }

    private fun showNetworkBanner(noConnectivity: Boolean) {
        runOnUiThread {
            binding.noInternetBanner.networkBanner.visibility = if (noConnectivity) View.VISIBLE else View.GONE
            binding.noInternetData.noDataNetworkLayout.visibility = if (noConnectivity) View.VISIBLE else View.GONE

        }
    }

    private fun refreshCurrentFragment() {
        navController.currentDestination?.let { destination ->
            navController.popBackStack(destination.id, true)
            navController.navigate(destination.id)
        }
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