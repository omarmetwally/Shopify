package com.omarinc.shopify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.omarinc.shopify.home.view.MainActivity
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.splashscreen.viewmodel.SplashNavigationState
import com.omarinc.shopify.splashscreen.viewmodel.SplashScreenViewModel
import com.omarinc.shopify.splashscreen.viewmodel.SplashScreenViewModelFactory
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val SPLASH_DELAY = 3000L

class SplashScreenFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val factory = SplashScreenViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(SplashScreenViewModel::class.java)

        lifecycleScope.launch {
            viewModel.navigationState.collect { state ->
                when (state) {
                    is SplashNavigationState.Checking -> {
                    }

                    is SplashNavigationState.NavigateToMain -> {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }

                    is SplashNavigationState.NavigateToLogin -> {
                        findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
                    }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkUserState()
        }, SPLASH_DELAY)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SplashScreenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
