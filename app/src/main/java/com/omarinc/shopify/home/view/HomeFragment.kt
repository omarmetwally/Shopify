package com.omarinc.shopify.home.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.omarinc.shopify.models.CouponDisplay
import com.omarinc.shopify.models.PrerequisiteToEntitlementPurchase
import com.omarinc.shopify.models.PrerequisiteToEntitlementQuantityRatio
import com.omarinc.shopify.models.PriceRule
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
    private lateinit var viewModel: HomeViewModel
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var adsAdapter: AdsAdapter


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


        binding.searchView.setOnClickListener {
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

    private fun setUpAdsAdapter() {
        adsAdapter = AdsAdapter(requireContext()) { priceRule ->
            onCouponLongClick(priceRule)
        }
        binding.adsVP.adapter = adsAdapter

        val images = listOf(
            R.drawable.coupon_1,
            R.drawable.coupon_2,

        )

        viewModel.getCoupons()

        lifecycleScope.launch {
            viewModel.coupons.collect { result ->
                when (result) {
                    is ApiState.Failure -> {
                        // Handle failure state if needed
                        Log.e(TAG, "Failed to fetch coupons: ${result.msg}")
                    }
                    ApiState.Loading -> {
                        // Handle loading state if needed
                        Log.d(TAG, "Fetching coupons...")
                    }
                    is ApiState.Success -> {
                        val coupons = result.response.price_rules
                        if (coupons.size == images.size) {
                            // Combine coupons with images
                            val couponDisplays = coupons.zip(images) { priceRule, image ->
                                CouponDisplay(priceRule, image)
                            }

                            // Submit the combined data to the adapter
                            adsAdapter.submitList(couponDisplays)

                            Log.d(TAG, "Coupons fetched successfully")
                        } else {
                            Log.e(TAG, "Number of coupons and images do not match")
                        }
                    }
                }
            }
        }
    }


    private fun onCouponLongClick(priceRule: PriceRule) {
        getCouponDetails(priceRule.id.toString())

        Log.i(TAG, "onCouponLongClick: ${priceRule.id.toString()}")
    }

    private fun getCouponDetails(couponId: String) {
        viewModel.getCouponDetails(couponId)

        lifecycleScope.launch {
            viewModel.couponDetails.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getCouponsDetails: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCouponsDetails: Loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "getCouponsDetails: ${result.response}")
                        // Copy the discount code to clipboard
                        val discountCode = result.response.discount_codes.firstOrNull()?.code ?: ""
                        val clipboard =
                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Discount Code", discountCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            requireContext(),
                            "Discount code copied: $discountCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
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
                        // Update adapter with coupons
                        setUpAdsAdapter()
                    }
                }
            }
        }
    }
}



