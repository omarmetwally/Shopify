package com.omarinc.shopify.home.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.ProductsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.home.view.adapters.AdsAdapter
import com.omarinc.shopify.home.view.adapters.BrandsAdapter
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var brandsManager: LinearLayoutManager
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var adsAdapter: AdsAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setViewModel()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.searchView.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)

        }

        binding.menu.setOnClickListener {
            if (binding.root.isDrawerOpen(GravityCompat.START)) {
                binding.root.closeDrawer(GravityCompat.START)
            } else {
                binding.root.openDrawer(GravityCompat.START)
            }
        }

        setUpAdsAdapter()
        setUpBrandsAdapter()
        setUpProductsAdapter()
        getCoupons()
        getCouponDetails()
    }

    private fun setUpProductsAdapter() {
        productsAdapter = ProductsAdapter(requireContext()) { productId ->
            val action =
                HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        productsManager = GridLayoutManager(requireContext(), 2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.productsRv.layoutManager = productsManager
        binding.productsRv.adapter = productsAdapter

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
    }

    private fun setUpBrandsAdapter() {
        val onBrandClick = { it: String ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductsFragment(
                    it
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        brandsAdapter = BrandsAdapter(
            requireContext(),
            onBrandClick
        )

        brandsManager = LinearLayoutManager(requireContext())
        brandsManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.brandsRV.layoutManager = brandsManager
        binding.brandsRV.adapter = brandsAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            brandsAdapter.submitList(result.response)
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }

    }

    private fun setUpAdsAdapter() {

        adsAdapter = AdsAdapter(requireContext())

        binding.adsVP.adapter = adsAdapter
        val images = listOf(
            R.drawable.shoe,
            R.drawable.discount,
            R.drawable.shoe,
            R.drawable.discount,
        )

        adsAdapter.submitList(images)
        binding.adsVP.setPageTransformer(ZoomOutPageTransformer())
    }

    private fun setViewModel() {
        val factory = HomeViewModel.HomeViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            )
        )

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        viewModel.getBrands()
        viewModel.getProductsByBrandId("gid://shopify/Collection/308805107891")
    }

    private fun getCoupons() {
        viewModel.getCoupons()

        lifecycleScope.launch {
            viewModel.coupons.collect { result ->


                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getCoupons: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCoupons: Loading")
                    is ApiState.Success -> {

                        Log.i(TAG, "getCoupons: ${result.response}")
                    }
                }

            }
        }
    }

    private fun getCouponDetails(){
        lifecycleScope.launch {
            viewModel.getCouponDetails("1119384043699")
            viewModel.couponDetails.collect{result->

                when(result){
                    is ApiState.Failure -> Log.i(TAG, "getCouponDetails: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCouponDetails: Lodaing")
                    is ApiState.Success -> {

                        Log.i(TAG, "getCouponDetails: ${result.response}")
                    }
                }

            }

        }
    }


}