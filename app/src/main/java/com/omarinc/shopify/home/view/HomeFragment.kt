package com.omarinc.shopify.home.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.omarinc.shopify.home.view.adapters.ProductsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
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
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
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





        checkIfIsFirstUserTime(view)

        setUpBrandsAdapter()
        setUpProductsAdapter()
        getCoupons()
        collectProducts()

    }

    private fun checkIfIsFirstUserTime(view: View) {
        val isFirstTimeUser: Deferred<Boolean> = lifecycleScope.async {
            viewModel.readIsFirstTimeUser("isFirst")
        }

        lifecycleScope.launch {
            if (!isFirstTimeUser.await()) {
                setupTabTargetPrompt(view)
            }
        }
    }

    private fun setupTabTargetPrompt(view: View) {
        val viewsToDisable = listOf(
            R.id.filter_view,
            R.id.homeFragment, R.id.categoriesFragment, R.id.shoppingCartFragment,
            R.id.search_view
        )

        view.post {
            saveToSharedPref()
            setViewsEnabled(viewsToDisable, false)

            showPrompt(
                targetId = R.id.filter_view,
                primaryText = "This is Fab",
                secondaryText = "Changing Prompt Style",
                backgroundColor = R.color.primary_color,
                focal = RectanglePromptFocal(),
                background = RectanglePromptBackground(),
                onFocalPressed = {
                    showPrompt(
                        targetId = R.id.search_view,
                        primaryText = "Button 2",
                        secondaryText = "Changing Focal Color",
                        backgroundColor = R.color.dark_grey,
                        focalColor = R.color.primary_color,
                        onFocalPressed = {
                            showPrompt(
                                targetId = R.id.homeFragment,
                                primaryText = "Button 1",
                                secondaryText = "Changing Focal Color",
                                backgroundColor = R.color.primary_color,
                                focalColor = R.color.white,
                                focalRadius = 150.4f,
                                onFocalPressed = {
                                    showPrompt(
                                        targetId = R.id.categoriesFragment,
                                        primaryText = "This is Fab",
                                        secondaryText = "Changing Prompt Style",
                                        backgroundColor = R.color.primary_color,
                                        focal = RectanglePromptFocal(),
                                        background = RectanglePromptBackground(),
                                        onFocalPressed = {
                                            Toast.makeText(
                                                requireContext(),
                                                "Hello",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            showPrompt(
                                                targetId = R.id.shoppingCartFragment,
                                                primaryText = "This is Fab",
                                                secondaryText = "Changing Prompt Style",
                                                backgroundColor = R.color.primary_color,
                                                focal = RectanglePromptFocal(),
                                                background = RectanglePromptBackground(),
                                                onFocalPressed = {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Hello",
                                                        Toast.LENGTH_LONG
                                                    ).show()
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
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                    onFocalPressed()
                }
            }
            .show()
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
                viewModel.productsApiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
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
        Log.i(TAG, "setUpAdsAdapter: 1")
        adsAdapter = AdsAdapter(requireContext()) { priceRule ->
            onCouponLongClick(priceRule)
        }
        binding.adsVP.adapter = adsAdapter

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
            // Combine currencyUnit and requiredCurrency flows
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
                        // Call your method to set up products adapter or any other necessary logic
                        requiredCurrency.response.data[currencyUnit]?.let { currency ->
                            Log.i(TAG, "getCurrentCurrency: ${currency.value}")
                            productsAdapter.updateCurrentCurrency(currency.value, currency.code)
                        }
                    }
                }
            }
        }
    }

}


