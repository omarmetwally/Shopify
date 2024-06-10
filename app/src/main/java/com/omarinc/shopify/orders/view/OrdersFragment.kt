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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.OrdersAdapter
import com.omarinc.shopify.databinding.FragmentOrdersBinding
import com.omarinc.shopify.home.view.HomeFragmentDirections
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.orders.viewmodel.OrdersViewModel
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch


class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private lateinit var ordersManager: LinearLayoutManager
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var viewModel: OrdersViewModel
    private lateinit var repo: ShopifyRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setUpViewModel()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       collectOrders()

       setUpOrdersAdapter()

    }

    private fun setUpOrdersAdapter() {
        ordersAdapter = OrdersAdapter(
            requireContext()
        ) { index ->
            val action =
                OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(index)
            findNavController().navigate(action)
        }

        ordersManager = LinearLayoutManager(requireContext())
        ordersManager.orientation = LinearLayoutManager.VERTICAL
        binding.ordersRV.layoutManager = ordersManager
        binding.ordersRV.adapter = ordersAdapter
    }

    private fun collectOrders() {
        lifecycleScope.launch {
            viewModel
                .getCutomerOrders(repo.readUserToken())
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            ordersAdapter.submitList(result.response)
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }
    }

    private fun setUpViewModel() {
        repo = ShopifyRepositoryImpl(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(), AdminRemoteDataSourceImpl.getInstance()
        )

        val factory = OrdersViewModel.OrdersViewModelFactory(
            repo
        )

        viewModel = ViewModelProvider(requireActivity(), factory).get(OrdersViewModel::class.java)
    }

}