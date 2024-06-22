package com.omarinc.shopify.address.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.FragmentAddressesBinding
import com.omarinc.shopify.address.view.adpters.AddressesAdapter
import com.omarinc.shopify.address.viewModel.AddressViewModel
import com.omarinc.shopify.address.viewModel.AddressViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class AddressesFragment : Fragment() {

    companion object {
        private const val TAG = "AddressesFragment"
    }

    private var _binding: FragmentAddressesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        setUpViewModel()
        getAddresses()
        setupListeners()

    }


    private fun setUpViewModel() {

        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory = AddressViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddressViewModel::class.java]

    }

    private fun getAddresses() {
        viewModel.getCustomersAddresses()

        lifecycleScope.launch {
            viewModel.addressList.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getAddresses: Failure ${result.msg}")
                    ApiState.Loading -> {
                        binding.addressesShimmer.startShimmer()
                    }
                    is ApiState.Success -> {
                        binding.addressesShimmer.stopShimmer()
                        binding.addressesShimmer.visibility = View.GONE

                        setupRecyclerView(result.response ?: emptyList())
                    }
                }

            }
        }
    }

    private fun deleteAddress(addressId: String) {

        viewModel.deleteAddress(addressId)

        lifecycleScope.launch {
            viewModel.addressDelete.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "deleteAddress: Failure ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "deleteAddress: Loading")
                    is ApiState.Success -> {
                        getAddresses()
                    }
                }

            }
        }
    }

    private fun setupRecyclerView(items: List<CustomerAddress?>) {
        val adapter = AddressesAdapter(requireActivity(), items) { addressId ->
            deleteAddress(addressId)
        }
        binding.addressesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            this.adapter = adapter
        }
        adapter.notifyDataSetChanged()
    }


    private fun setupListeners() {
        binding.goToMapButton.setOnClickListener {

            val action = AddressesFragmentDirections.actionAddressesFragmentToMapFragment()
            findNavController().navigate(action)
        }
    }

}