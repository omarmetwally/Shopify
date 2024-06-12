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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo3.api.Optional
import com.google.android.material.chip.Chip
import com.omarinc.shopify.R
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel
import com.omarinc.shopify.databinding.FragmentProductDetailsBinding
import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModel
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.CART_ID
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProductDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ProductDetailsFragment()
        const val TAG = "ProductDetailsFragment"
    }

    private lateinit var sharedPreferences: SharedPreferencesImpl
    private lateinit var viewModel: ProductDetailsViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var binding: FragmentProductDetailsBinding

    private lateinit var productId: String
    private lateinit var variantId: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModel()

        productId = arguments?.getString("productId") ?: ""

        val userToken = sharedPreferences.readStringFromSharedPreferences(
            Constants.USER_TOKEN
        )

        loadProductDetails(productId)




        setListeners()

        checkFavorite(userToken, productId)



        getCurrentCurrency()

        clickFavorite(userToken, productId)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }


    private fun setUpViewModel() {


        sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )

        val productDetailsFactory =
            ProductDetailsViewModelFactory(repository, FirebaseRepository.getInstance())

        viewModel =
            ViewModelProvider(this, productDetailsFactory).get(ProductDetailsViewModel::class.java)


        val favoriteFactory = FavoriteViewModelFactory(FirebaseRepository.getInstance())

        favoriteViewModel =
            ViewModelProvider(this, favoriteFactory).get(FavoriteViewModel::class.java)


    }

    private fun clickFavorite(userToken: String, productId: String) {
        binding.btnFavorite.setOnClickListener {
            lifecycleScope.launch {
                val isFavorite = favoriteViewModel.isFavorite.value
                if (isFavorite) {
                    showUnfavoriteDialog(userToken, productId)
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

    private fun showUnfavoriteDialog(userToken: String, productId: String) {
        Helper.showAlertDialog(
            context = requireContext(),
            title = getString(R.string.unfavorite_product),
            message = getString(R.string.are_you_sure_to_unfavorite),
            positiveButtonText = getString(R.string.yes),
            positiveButtonAction = {
                favoriteViewModel.removeFromFavorites(userToken, productId)
            },
            negativeButtonText = getString(R.string.no)
        )
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
                        variantId = productDetails.variants.firstOrNull()?.id.toString()
                        if (variantId != null) {
                            Log.i(TAG, "Variant ID: $variantId")
                        }

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
                        stopAnimations()

                    }

                    is ApiState.Failure -> {
                    }

                    ApiState.Loading -> {
                    }
                }
            }
        }
    }

    private fun stopAnimations() {
        binding.imagesShimmer.hideShimmer()
        binding.imagesShimmer.stopShimmer()
        binding.deatilsShimmer.hideShimmer()
        binding.deatilsShimmer.stopShimmer()
        binding.reviewsShimmer.hideShimmer()
        binding.reviewsShimmer.stopShimmer()
    }

    private fun setListeners() {
        binding.btnAddToCart.setOnClickListener {

            val email: Deferred<String> = lifecycleScope.async {
                viewModel.readCustomerEmail()
            }

            lifecycleScope.launch {
                val userEmail = email.await()
                Log.i(TAG, "Email: $userEmail")

                viewModel.isCustomerHasCart(userEmail)

                viewModel.hasCart.collect { result ->
                    when (result) {
                        is ApiState.Failure -> Log.i(TAG, "hasCart Failure: ${result.msg}")
                        ApiState.Loading -> Log.i(TAG, "hasCart Loading: ")
                        is ApiState.Success -> {
                            Log.i(TAG, "hasCart Success user has cart: ${result.response}")
                            if (!result.response) {

                                Log.i(TAG, "hasCart: ${result.response}")

                                val cartId = createNewCart(userEmail)
                                sharedPreferences.writeStringToSharedPreferences(CART_ID, cartId)
                               // viewModel.writeCartId(cartId)
                                // val variantId = getFirstVariantId(productId)

                                Log.i(TAG, "variantId 11: $variantId")

                                if (variantId != null) {

                                    Log.i(TAG, "Added to Cart ID: $cartId")
                                    addProductToCart(
                                        cartId,
                                        1, variantId
                                    )

                                } else {
                                    Log.e(TAG, "Variant ID not found for product.")
                                }
                            } else {
                                viewModel.getCartByCustomer(userEmail)
                                viewModel.customerCart.collect { result ->
                                    when (result) {
                                        is ApiState.Failure -> Log.i(
                                            TAG,
                                            "customerCart Failure: ${result.msg}"
                                        )

                                        ApiState.Loading -> Log.i(TAG, "Loading")
                                        is ApiState.Success -> {

                                            Log.i(TAG, "CartId : ${result.response} ")
                                            val cartId = result.response
                                           // viewModel.writeCartId(cartId!!)
                                            sharedPreferences.writeStringToSharedPreferences(
                                                CART_ID,
                                                cartId!!
                                            )
                                            Log.i(TAG, "customerCart Success: $cartId")

                                            // val variantId = getFirstVariantId(productId)
                                            if (variantId != null) {
                                                Log.i(TAG, "customerCart2: variantId $variantId")
                                                addProductToCart(
                                                    cartId ?: "",
                                                    1, variantId
                                                )
                                            } else {
                                                Log.e(TAG, "Variant ID not found for product.")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private suspend fun createNewCart(email: String): String {
        viewModel.createCart(email)

        var cartId: String? = null

        viewModel.cartId
            .filterIsInstance<ApiState.Success<String>>()
            .firstOrNull()?.let { result ->
                cartId = result.response
            }

        cartId?.let {
            viewModel.addCustomerCart(email, it)
            return it
        } ?: throw IllegalStateException("Cart ID could not be retrieved")
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
                        ColorStateList.valueOf(getResources().getColor(R.color.secondary_color))
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


    private fun addProductToCart(cartId: String, quantity: Int, variantId: String) {
        viewModel.addProductToCart(cartId, quantity, variantId)

        lifecycleScope.launch {
            viewModel.addingToCart.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        val addedCartId = state
                        Log.i(
                            TAG,
                            "Product added to cart successfully. Cart ID: ${addedCartId.response}"
                        )
                    }

                    is ApiState.Failure -> {
                        Log.e(TAG, "Failed to add product to cart: ${state.msg}")
                    }

                    ApiState.Loading -> {
                        Log.i(TAG, "Adding product to cart...")
                    }
                }
            }
        }
    }


    private fun getCurrentCurrency() {

        viewModel.getRequiredCurrency()
        lifecycleScope.launch {
            viewModel.requiredCurrency.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getCurrentCurrency: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCurrentCurrency: Loading")
                    is ApiState.Success -> Log.i(
                        TAG,
                        "getCurrentCurrency: ${result.response.data.values}"
                    )
                }

            }
        }
    }
}