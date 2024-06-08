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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.OrdersAdapter
import com.omarinc.shopify.databinding.FragmentOrdersBinding
import com.omarinc.shopify.home.view.HomeFragmentDirections
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
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

         repo = ShopifyRepositoryImpl(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance())
        val factory = OrdersViewModel.OrdersViewModelFactory(
            repo
        )

        viewModel = ViewModelProvider(this,factory).get(OrdersViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrdersBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launch {
            viewModel
                .getCutomerOrders(repo.readUserToken())
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            ordersAdapter.submitList(result.response)
                        }
                        is ApiState.Failure ->{

                        }
                    }
                }
            }
        }


        val onBrandClick = { id : String ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductsFragment(
                    id
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        ordersAdapter = OrdersAdapter(
            requireContext(),
        )

        ordersManager = LinearLayoutManager(requireContext())
        ordersManager.orientation = LinearLayoutManager.VERTICAL
        binding.ordersRV.layoutManager = ordersManager
        binding.ordersRV.adapter = ordersAdapter

    }
}