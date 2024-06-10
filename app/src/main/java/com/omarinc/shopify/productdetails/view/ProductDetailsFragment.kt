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

        val productId = arguments?.getString("productId") ?: ""

        val userToken = sharedPreferences.readStringFromSharedPreferences(
            Constants.USER_TOKEN
        )

        loadProductDetails(productId)


        /*viewModel.isCustomerHasCart("testdfdf@test.com")

        lifecycleScope.launch {

            viewModel.hasCart.collect { result ->

                when(result){
                    is ApiState.Failure -> Log.i(TAG, "hasCart Failure: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "hasCart Loading: ")
                    is ApiState.Success -> Log.i(TAG, "hasCart Success: ${result.response} ")
                }
            }
        }*/


        setListeners()


        /*        viewModel.createCart("test@test.com")

                lifecycleScope.launch {

                    viewModel.cartId.collect { result ->

                        when (result) {
                            is ApiState.Failure -> Log.i(TAG, "onViewCreated: failure ${result.msg}")
                            ApiState.Loading -> Log.i(TAG, "onViewCreated: Loading")
                            is ApiState.Success -> Log.i(
                                TAG,
                                "onViewCreated: Success ${result.response}"
                            )
                        }

                    }
                }*/

        checkFavorite(userToken, productId)

        lifecycleScope.launch {


            Log.i(TAG, "onViewCreated: Email ${viewModel.readCustomerEmail()}")
        }

        getCurrentCurrency()

        clickFavorite(userToken, productId)


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


    private fun setListeners() {
        Log.i(TAG, "setListeners: ")

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnAddToCart.setOnClickListener {

            val email: Deferred<String>
            email = lifecycleScope.async {
                viewModel.readCustomerEmail()
            }


            lifecycleScope.launch {

                viewModel.isCustomerHasCart(email.await())
                Log.i(TAG, "Email: ${email.await()}")
                viewModel.hasCart.collect { result ->
                    when (result) {
                        is ApiState.Failure -> Log.i(TAG, "hasCart Failure: ${result.msg}")
                        ApiState.Loading -> Log.i(TAG, "hasCart Loading: ")
                        is ApiState.Success -> {
                            Log.i(TAG, "hasCart Success: ${result.response}")
                            if (!result.response) {
                                val cartId = createNewCart(email.await())


                            } else {

                                viewModel.getCartByCustomer(email.await())
                                viewModel.customerCart.collect { result ->
                                    when (result) {
                                        is ApiState.Failure -> Log.i(
                                            TAG,
                                            "cutomerCart Failure: ${result.msg}"
                                        )

                                        ApiState.Loading -> Log.i(TAG, "Loading ")
                                        is ApiState.Success -> {

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

        // Variable to hold the cartId
        var cartId: String? = null

        // Collect only one result from the flow
        viewModel.cartId
            .filterIsInstance<ApiState.Success<String>>()
            .firstOrNull()?.let { result ->
                cartId = result.response
            }

        // Make sure the cartId is not null before proceeding
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