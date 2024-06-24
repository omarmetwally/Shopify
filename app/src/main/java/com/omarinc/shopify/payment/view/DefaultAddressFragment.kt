package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentDefaultAddressBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.payment.viewModel.PaymentViewModel
import com.omarinc.shopify.payment.viewModel.PaymentViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class DefaultAddressFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "DefaultAddressFragment"
    }

    private var _binding: FragmentDefaultAddressBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PaymentViewModel
    private lateinit var checkoutId: String
    private var totalPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDefaultAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkoutId = arguments?.getString("checkoutId") ?: ""
        totalPrice = arguments?.getString("totalPrice")?.toDouble() ?: 0.0
        setupViewModel()
        getCustomerAddresses()
    }

    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory = PaymentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PaymentViewModel::class.java)
    }

    private fun getCustomerAddresses() {
        viewModel.getCustomersAddresses()
        lifecycleScope.launch {
            viewModel.addressList.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getCustomerAddresses: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCustomerAddresses: loading")
                    is ApiState.Success -> {
                        setupRecyclerView(result.response ?: emptyList())
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(items: List<CustomerAddress?>) {
        val adapter = DefaultAddressAdapter(items, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedAddress = items[position]
                val action = DefaultAddressFragmentDirections.actionDefaultAddressFragmentToPaymentFragment(checkoutId,totalPrice.toString(),selectedAddress)


                findNavController().navigate(action, navOptions {
                    popUpTo(R.id.paymentFragment) {
                        inclusive = true
                    }
                })            }
        })
        binding.addressesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            this.adapter = adapter
        }
        adapter.notifyDataSetChanged()
    }
}
