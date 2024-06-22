package com.omarinc.shopify.orders.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omarinc.shopify.home.view.adapters.ProductsAdapter
import com.omarinc.shopify.databinding.FragmentOrderDetailsBinding
import com.omarinc.shopify.home.view.HomeFragmentDirections
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.orders.viewmodel.OrdersViewModel
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch


class OrderDetailsFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var viewModel: OrdersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }
        setUpProductsAdapter()
        collectOrderDetails()
    }



    private fun setUpViewModel() {

        val factory = OrdersViewModel.OrdersViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(), AdminRemoteDataSourceImpl.getInstance()
            )
        )

        viewModel = ViewModelProvider(requireActivity(), factory).get(OrdersViewModel::class.java)
    }

    private fun setUpProductsAdapter() {
        productsAdapter = ProductsAdapter(requireContext()) { productId ->
            val action =
                HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        productsManager = GridLayoutManager(requireContext(), 2)
        productsManager.orientation = GridLayoutManager.VERTICAL
        binding.orderProductsRv.layoutManager = productsManager
        binding.orderProductsRv.adapter = productsAdapter


    }

    private fun collectOrderDetails(){
        val index = arguments?.getInt("index") ?: 0
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            binding.orderId.text = result.response[index].name
                            binding.address.text = result.response[index].address?:"October,Giza,Egypt"
                            binding.phone.text = result.response[index].phone?:"+201127108998"
                            binding.itemsCount.text = "${result.response[index].products.count()} items"
                            binding.tax.text = "${result.response[index].totalTaxAmount} EGP"
                            binding.subTotal.text = "${result.response[index].subTotalPriceAmount} EGP"
                            binding.totalPrice.text = "${result.response[index].totalPriceAmount} EGP"
                            productsAdapter.submitList(result.response[index].products)
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }
    }

}