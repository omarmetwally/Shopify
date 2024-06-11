package com.omarinc.shopify.map.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentAddressDetailsBinding
import com.omarinc.shopify.map.viewModel.AddressViewModel
import com.omarinc.shopify.map.viewModel.AddressViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl


class AddressDetailsFragment : Fragment() {

    private var _binding: FragmentAddressDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddressDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

    }


    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val factory = AddressViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AddressViewModel::class.java]
    }
}