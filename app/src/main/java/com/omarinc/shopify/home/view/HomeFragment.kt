package com.omarinc.shopify.home.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.omarinc.shopify.AuthenticationMainActivity
import com.omarinc.shopify.home.view.adapters.ProductsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.home.view.adapters.AdsAdapter
import com.omarinc.shopify.home.view.adapters.BrandsAdapter
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CouponDisplay
import com.omarinc.shopify.models.PriceRule
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.view.ProductDetailsFragment
import com.omarinc.shopify.productdetails.view.ProductDetailsFragment.Companion
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.CART_ID
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal


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
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: ISharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
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

        binding.favourites.setOnClickListener {
            if (checkUserTokenExist() == "null") {

                showGuestModeAlertDialog()
            } else {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToFavoritesFragment()
                findNavController().navigate(action)
            }

        }

        binding.searchView.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)

        }


        checkIfIsFirstUserTime(view)
        setUpBrandsAdapter()
        setUpProductsAdapter()
        getCoupons()
        collectProducts()
        createCustomerCart()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.drawer_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun checkIfIsFirstUserTime(view: View) {
        val isFirstTimeUser: Deferred<Boolean> = lifecycleScope.async {
            viewModel.readIsFirstTimeUser("isFirst")
        }

        lifecycleScope.launch {
            if (!isFirstTimeUser.await()) {
                setupTabTargetPrompt(view)
                saveToSharedPref()
            }
        }
    }

    private val viewsToDisable = listOf(
        R.id.favourites,
        R.id.homeFragment, R.id.categoriesFragment, R.id.shoppingCartFragment,
        R.id.search_view, R.id.profileFragment
    )

    private fun setupTabTargetPrompt(view: View) {

        view.post {
            setViewsEnabled(viewsToDisable, false)

            showPrompt(
                targetId = R.id.favourites,
                primaryText = getString(R.string.favourites_primary_text),
                secondaryText = getString(R.string.favourites_secondary_text),
                backgroundColor = R.color.primary_color,
                focal = RectanglePromptFocal(),
                background = RectanglePromptBackground(),
                onFocalPressed = {
                    showPrompt(
                        targetId = R.id.search_view,
                        primaryText = getString(R.string.search_primary_text),
                        secondaryText = getString(R.string.search_secondary_text),
                        backgroundColor = R.color.primary_color,
                        focalColor = R.color.white,
                        onFocalPressed = {
                            showPrompt(
                                targetId = R.id.ads_placeholder,
                                primaryText = getString(R.string.discount_primary_text),
                                secondaryText = getString(R.string.discount_secondary_text),
                                backgroundColor = R.color.primary_color,
                                focalColor = R.color.white,
                                focalRadius = 150.4f,
                                onFocalPressed = {
                                    showPrompt(
                                        targetId = R.id.categoriesFragment,
                                        primaryText = getString(R.string.categories_primary_text),
                                        secondaryText = getString(R.string.categories_secondary_text),
                                        backgroundColor = R.color.primary_color,
                                        focal = RectanglePromptFocal(),
                                        background = RectanglePromptBackground(),
                                        onFocalPressed = {
                                            /*Toast.makeText(
                                                requireContext(),
                                                "Hello",
                                                Toast.LENGTH_LONG
                                            ).show()*/
                                            showPrompt(
                                                targetId = R.id.shoppingCartFragment,
                                                primaryText = getString(R.string.cart_secondary_text),
                                                secondaryText = getString(R.string.cart_secondary_text),
                                                backgroundColor = R.color.primary_color,
                                                focal = RectanglePromptFocal(),
                                                background = RectanglePromptBackground(),
                                                onFocalPressed = {
                                                    showPrompt(
                                                        targetId = R.id.profileFragment,
                                                        primaryText = getString(R.string.profile_primary_text),
                                                        secondaryText = getString(R.string.profile_secondary_text),
                                                        backgroundColor = R.color.primary_color,
                                                        focal = RectanglePromptFocal(),
                                                        background = RectanglePromptBackground(),
                                                        onFocalPressed = {
                                                            //setViewsEnabled(viewsToDisable,true)
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
        setViewsEnabled(viewsToDisable, true)

    }

    private fun saveToSharedPref() {
        lifecycleScope.launch {
            viewModel.writeIsFirstTimeUser("isFirst", true)
        }
    }

    private fun setViewsEnabled(viewIds: List<Int>, enabled: Boolean) {
        viewIds.forEach { id ->
            requireActivity().findViewById<View>(id)?.isEnabled = enabled
        }
    }

    private fun showPrompt(
        targetId: Int,
        primaryText: String,
        secondaryText: String,
        backgroundColor: Int,
        focalColor: Int? = null,
        focalRadius: Float? = null,
        focal: PromptFocal? = null,
        background: PromptBackground? = null,
        onFocalPressed: () -> Unit
    ) {
        MaterialTapTargetPrompt.Builder(requireActivity())
            .setTarget(targetId)
            .setPrimaryText(primaryText)
            .setSecondaryText(secondaryText)
            .setBackgroundColour(requireActivity().resources.getColor(backgroundColor))
            .apply {
                focalColor?.let { setFocalColour(requireActivity().resources.getColor(it)) }
                focalRadius?.let { setFocalRadius(it) }
                focal?.let { setPromptFocal(it) }
                background?.let { setPromptBackground(it) }
            }
            .setPromptStateChangeListener { prompt, state ->
                when (state) {
                    MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> onFocalPressed()
                    MaterialTapTargetPrompt.STATE_DISMISSED -> onPromptDismissed()
                }
            }
            .show()
    }

    private fun onPromptDismissed() {
        setViewsEnabled(viewsToDisable, true)
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


    }

    private fun collectProducts() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {
                            binding.homeScrollView.visibility = View.GONE
                            binding.homeShimmer.startShimmer()
                        }

                        is ApiState.Success -> {
                            binding.homeScrollView.visibility = View.VISIBLE
                            binding.homeShimmer.stopShimmer()
                            binding.homeShimmer.visibility = View.GONE
                            productsAdapter.submitList(result.response)
                            getCurrentCurrency()
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
                viewModel.brandsState.collect { result ->
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
            ),
            FirebaseRepository.getInstance()
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

        val dotsIndicator = binding.dotsIndicator
        dotsIndicator.attachTo(binding.adsVP)

        val images = listOf(
            R.drawable.coupon_1,
            R.drawable.coupon_2,
        )

        lifecycleScope.launch {
            viewModel.coupons.collect { result ->
                when (result) {
                    is ApiState.Failure -> {
                        Log.e(TAG, "Failed to fetch coupons: ${result.msg}")
                    }

                    ApiState.Loading -> {
                        Log.d(TAG, "Fetching coupons...")
                    }

                    is ApiState.Success -> {
                        Log.i(TAG, "setUpAdsAdapter: Success")
                        val coupons = result.response.price_rules
                        if (coupons.size == images.size) {
                            val couponDisplays = coupons.zip(images) { priceRule, image ->
                                CouponDisplay(priceRule, image)
                            }
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


    private fun getCurrentCurrency() {
        viewModel.getCurrencyUnit()
        viewModel.getRequiredCurrency()

        lifecycleScope.launch {
            combine(
                viewModel.currencyUnit,
                viewModel.requiredCurrency
            ) { currencyUnit, requiredCurrency ->
                Pair(currencyUnit, requiredCurrency)
            }.collect { (currencyUnit, requiredCurrency) ->
                Log.i(TAG, "getCurrentCurrency 000: $currencyUnit")
                when (requiredCurrency) {
                    is ApiState.Failure -> Log.i(TAG, "getCurrentCurrency: ${requiredCurrency.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCurrentCurrency: Loading")
                    is ApiState.Success -> {
                        Log.i(
                            TAG,
                            "getCurrentCurrency: ${requiredCurrency.response.data[currencyUnit]?.code}"
                        )
                        requiredCurrency.response.data[currencyUnit]?.let { currency ->
                            Log.i(TAG, "getCurrentCurrency: ${currency.value}")
                            productsAdapter.updateCurrentCurrency(currency.value, currency.code)
                        }
                    }
                }
            }
        }
    }


    private fun checkUserTokenExist(): String {
        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())

        return sharedPreferences.readStringFromSharedPreferences(Constants.USER_TOKEN)
    }

    private fun showGuestModeAlertDialog() {
        Helper.showAlertDialog(
            context = requireContext(),
            title = getString(R.string.guest_mode),
            message = getString(R.string.guest_mode_message),
            positiveButtonText = getString(R.string.login),
            positiveButtonAction = {
                invertSkippedFlag()
                navigateToLogin()
            },
            negativeButtonText = getString(R.string.no)
        )
    }

    private fun invertSkippedFlag() {
        sharedPreferences.writeBooleanToSharedPreferences(Constants.USER_SKIPPED, false)
    }

    private fun navigateToLogin() {

        startActivity(Intent(requireContext(), AuthenticationMainActivity::class.java))
        requireActivity().finish()

    }

    private fun createCustomerCart() {

        val email: Deferred<String> = lifecycleScope.async {
            viewModel.readCustomerEmail()
        }

        lifecycleScope.launch {

            val userEmail = email.await()
            viewModel.isCustomerHasCart(userEmail)

            viewModel.hasCart.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.i(ProductDetailsFragment.TAG, "hasCart Failure: ${result.msg}")
                    ApiState.Loading -> Log.i(ProductDetailsFragment.TAG, "hasCart Loading: ")
                    is ApiState.Success -> {
                        Log.i(ProductDetailsFragment.TAG, "hasCart Success user has cart: ${result.response}")
                        if (!result.response) {

                            Log.i(ProductDetailsFragment.TAG, "hasCart: ${result.response}")

                                                    } else {
                            viewModel.getCartByCustomer(userEmail)
                            viewModel.customerCart.collect { result ->
                                when (result) {
                                    is ApiState.Failure -> Log.i(
                                        ProductDetailsFragment.TAG,
                                        "customerCart Failure: ${result.msg}"
                                    )

                                    ApiState.Loading -> Log.i(ProductDetailsFragment.TAG, "Loading")
                                    is ApiState.Success -> {

                                        Log.i(ProductDetailsFragment.TAG, "CartId : ${result.response} ")
                                        val cartId = result.response
                                        // viewModel.writeCartId(cartId!!)
                                        sharedPreferences.writeStringToSharedPreferences(
                                            CART_ID,
                                            cartId!!
                                        )
                                        Log.i(ProductDetailsFragment.TAG, "customerCart Success: $cartId")



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


