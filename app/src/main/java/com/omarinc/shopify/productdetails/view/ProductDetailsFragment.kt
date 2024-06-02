package com.omarinc.shopify.productdetails.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.omarinc.shopify.R
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel
import com.omarinc.shopify.databinding.FragmentProductDetailsBinding
import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.favorites.model.FavoritesRepository
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModel
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModelFactory
import com.omarinc.shopify.sharedpreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.launch

class ProductDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ProductDetailsFragment()
    }

    private lateinit var viewModel: ProductDetailsViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var binding: FragmentProductDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val productDetailsFactory = ProductDetailsViewModelFactory(repository)
        viewModel =
            ViewModelProvider(this, productDetailsFactory).get(ProductDetailsViewModel::class.java)

        val favoriteFactory = FavoriteViewModelFactory(FavoritesRepository.getInstance())
        favoriteViewModel =
            ViewModelProvider(this, favoriteFactory).get(FavoriteViewModel::class.java)


        loadProductDetails()

        val userToken = sharedPreferences.readStringFromSharedPreferences(
            Constants.USER_TOKEN
        )
        lifecycleScope.launch {
            favoriteViewModel.checkIfFavorite(
                userToken, "gid://shopify/Product/7880180302003"
            )
            favoriteViewModel.isFavorite.collect { isFavorite ->
                binding.btnFavorite.setImageResource(
                    if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_unfilled
                )
            }
        }
        binding.btnFavorite.setOnClickListener {
            lifecycleScope.launch {
                val isFavorite = favoriteViewModel.isFavorite.value
                if (isFavorite) {
                    favoriteViewModel.removeFromFavorites(
                        userToken,
                        "gid://shopify/Product/7880180302003"
                    )
                } else {
                    val favoriteItem = FavoriteItem(
                        productId = "gid://shopify/Product/7880180302003",
                        productName = binding.tvProductName.text.toString(),
                        productPrice = binding.tvProductPrice.text.toString().removeSuffix(" USD")
                            .toDouble(),
                        productImage = viewModel.apiState.value.let {
                            if (it is ApiState.Success) it.response.images[0].src else ""
                        }
                    )
                    favoriteViewModel.addToFavorites(userToken, favoriteItem)
                }
            }
        }
    }


    private fun loadProductDetails() {
        //        val productId = arguments?.getString("productId") ?: ""

        viewModel.getProductById("gid://shopify/Product/7880180302003")
        lifecycleScope.launch {
            viewModel.apiState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        val productDetails = state.response
                        Log.e("ProductDetailsFragment ", ": $productDetails")
                        binding.tvProductName.text = productDetails.title
                        binding.tvProductVendor.text = productDetails.vendor
                        binding.tvProductPrice.text = "${productDetails.price} USD"
                        binding.tvProductStock.text = "In Stock: ${productDetails.totalInventory}"
                        binding.tvProductDescription.text = productDetails.description

                        val imageUrls = productDetails.images.map { it.src }
                        val adapter = ImagesPagerAdapter(requireContext(), imageUrls)
                        binding.viewPagerImages.adapter = adapter

                        //                        TabLayoutMediator(binding.tabDots, binding.viewPagerImages) { _, _ -> }.attach()
                        val dotsIndicator = binding.dotsIndicator
                        dotsIndicator.attachTo(binding.viewPagerImages)

                        val staticComments = Helper.generateStaticComments()
                        val randomComments = Helper.getRandomComments(staticComments, 3)
                        val commentsAdapter = CommentsAdapter(randomComments)
                        binding.rvCustomerComments.adapter = commentsAdapter
                        binding.rvCustomerComments.layoutManager =
                            LinearLayoutManager(requireContext())
                    }

                    is ApiState.Failure -> {
                    }

                    ApiState.Loading -> {
                    }
                }
            }
        }
    }
}