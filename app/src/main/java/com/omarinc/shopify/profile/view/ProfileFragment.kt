package com.omarinc.shopify.profile.view

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.omarinc.shopify.AuthenticationMainActivity
import com.omarinc.shopify.profile.viewModel.ProfileViewModel
import com.omarinc.shopify.R
import com.omarinc.shopify.SplashScreenFragment
import com.omarinc.shopify.databinding.FragmentProfileBinding
import com.omarinc.shopify.home.view.MainActivity
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.profile.viewModel.ProfileViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel()
    }

    private fun setViewModel() {
        val factory = ProfileViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            )
        )
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setListeners()

    }

    private fun setListeners() {
        binding.settingsLinearLayout.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
        _binding?.orders?.setOnClickListener {
            val action = ProfileFragmentDirections
                .actionProfileFragmentToOrdersFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        _binding?.wishList?.setOnClickListener {

            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }
        _binding?.logOut?.setOnClickListener {

            showLogoutAlertDialog()
        }
    }

    private fun showLogoutAlertDialog() {
        Helper.showAlertDialog(
            context = requireContext(),
            title = getString(R.string.logout_pressed),
            message = getString(R.string.logout_pressed_message),
            positiveButtonText = getString(R.string.yes),
            positiveButtonAction = {
                logout()
            },
            negativeButtonText = getString(R.string.no)
        )
    }
    private fun logout() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.clearData()

            navigateToAuthintication()
        }

    }

    private fun navigateToAuthintication() {
        startActivity(Intent(requireActivity(), AuthenticationMainActivity::class.java))
        requireActivity().finish()
    }


}