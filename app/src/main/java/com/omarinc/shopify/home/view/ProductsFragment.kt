package com.omarinc.shopify.home.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.ProductsAdapter
import com.omarinc.shopify.databinding.FragmentProductsBinding
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedpreferences.SharedPreferencesImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var viewModel: HomeViewModel

    private val searchQuery = MutableStateFlow("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = HomeViewModel.HomeViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance()
            )
        )


        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        val id = arguments?.getString("id")
        Log.i("TAG", "onCreate: id" + id)

        viewModel.getProductsByBrandId(id ?: "gid://shopify/Collection/308804419763")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsAdapter = ProductsAdapter(requireContext()) { productId ->
            val action =
                ProductsFragmentDirections.actionProductsFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        productsManager = GridLayoutManager(requireContext(), 2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.productsRV.layoutManager = productsManager
        binding.productsRV.adapter = productsAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsApiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            productsAdapter.submitList(result.response)
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }

        setupSearchView()
        collectSearchQuery()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery.value = newText ?: ""
                return true
            }
        })
    }

    private fun collectSearchQuery() {
        lifecycleScope.launch {
            searchQuery.debounce(150)
                .distinctUntilChanged()
                .collect { query ->
                    filterProducts(query)
                }
        }
    }

    private fun filterProducts(query: String) {
        viewModel.productsApiState.value.let { state ->
            if (state is ApiState.Success) {
                val filteredList = state.response.filter { product ->
                    product.title.contains(query, ignoreCase = true)
                }
                productsAdapter.submitList(filteredList)
            }
        }
    }
}