package com.omarinc.shopify.productdetails.view

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
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
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.launch

class ProductDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ProductDetailsFragment()
        val TAG = "ProductDetailsFragment"
    }

    private lateinit var viewModel: ProductDetailsViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var binding: FragmentProductDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        val productId = arguments?.getString("productId") ?: ""
        val userToken = sharedPreferences.readStringFromSharedPreferences(
            Constants.USER_TOKEN
        )

        loadProductDetails(productId)

        checkFavorite(userToken, productId)
        getCurrentCurrency()
        clickFavorite(userToken, productId)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun clickFavorite(userToken: String, productId: String) {
        binding.btnFavorite.setOnClickListener {
            lifecycleScope.launch {
                val isFavorite = favoriteViewModel.isFavorite.value
                if (isFavorite) {
                    favoriteViewModel.removeFromFavorites(
                        userToken, productId
                    )
                } else {
                    val favoriteItem = FavoriteItem(productId = productId,
                        productName = binding.tvProductName.text.toString(),
                        productPrice = binding.tvProductPrice.text.toString().removeSuffix(" USD")
                            .toDouble(),
                        productImage = viewModel.apiState.value.let {
                            if (it is ApiState.Success) it.response.images[0].src else ""
                        })
                    favoriteViewModel.addToFavorites(userToken, favoriteItem)
                }
            }
        }
    }

    private fun checkFavorite(userToken: String, productId: String) {
        lifecycleScope.launch {
            favoriteViewModel.checkIfFavorite(
                userToken, productId
            )
            favoriteViewModel.isFavorite.collect { isFavorite ->
                binding.btnFavorite.setImageResource(
                    if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_unfilled
                )
            }
        }
    }


    private fun loadProductDetails(productId: String) {

        viewModel.getProductById(productId)
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

                        // TabLayoutMediator(binding.tabDots, binding.viewPagerImages) { _, _ -> }.attach()
                        val dotsIndicator = binding.dotsIndicator
                        dotsIndicator.attachTo(binding.viewPagerImages)

                        val staticComments = Helper.generateStaticComments()
                        val randomComments = Helper.getRandomComments(staticComments, 3)
                        val commentsAdapter = CommentsAdapter(randomComments)
                        binding.rvCustomerComments.adapter = commentsAdapter
                        binding.rvCustomerComments.layoutManager =
                            LinearLayoutManager(requireContext())
                        setupChips(productDetails)

                    }

                    is ApiState.Failure -> {
                    }

                    ApiState.Loading -> {
                    }
                }
            }
        }
    }

    private fun setupChips(productDetails: ProductDetails) {
        binding.chipGroupSizes.removeAllViews()
        binding.chipGroupColors.removeAllViews()

        val sizeSet = mutableSetOf<String>()
        val colorSet = mutableSetOf<String>()

        productDetails.variants.forEach { variant ->
            variant.selectedOptions.forEach { option ->
                when (option.name) {
                    "Size" -> sizeSet.add(option.value)
                    "Color" -> colorSet.add(option.value)
                }
            }
        }

        sizeSet.forEach { size ->
            val chip = Chip(context).apply {
                text = size.uppercase()
                isCheckable = true
                setOnCheckedChangeListener { buttonView, isChecked ->
                    (buttonView as Chip).chipBackgroundColor = if (isChecked) {
                        ColorStateList.valueOf( getResources().getColor(R.color.secondary_color))
                    } else {
                        ColorStateList.valueOf(Color.WHITE)
                    }
                }
            }
            binding.chipGroupSizes.addView(chip)
        }


        colorSet.forEach { color ->
            val chipView = LayoutInflater.from(context)
                .inflate(R.layout.layout_color_chip, binding.chipGroupColors, false)
            val chip = chipView.findViewById<Chip>(R.id.chip_color)
            val colorName = chipView.findViewById<TextView>(R.id.tv_color_name)

            chip.chipBackgroundColor = ColorStateList.valueOf(Helper.getColorFromName(color))
            chip.setOnCheckedChangeListener { _, isChecked ->
                chip.setChipIconVisible(isChecked)
            }
            colorName.text = color

            binding.chipGroupColors.addView(chipView)
        }
    }


    private fun getCurrentCurrency(){

        viewModel.getRequiredCurrency()
        lifecycleScope.launch {
            viewModel.requiredCurrency.collect {result->

                when(result){
                    is ApiState.Failure -> Log.i(TAG, "getCurrentCurrency: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCurrentCurrency: Loading")
                    is ApiState.Success -> Log.i(TAG, "getCurrentCurrency: ${result.response.data.values}")
                }

            }
        }
    }
}