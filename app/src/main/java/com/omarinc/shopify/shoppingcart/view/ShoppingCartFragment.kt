package com.omarinc.shopify.shoppingcart.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.FragmentShoppingCartBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModel
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModelFactory
import com.omarinc.shopify.type.CheckoutLineItemInput
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ShoppingCartFragment : Fragment() {

    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var viewModel: ShoppingCartViewModel
    private var productsLine = listOf<CheckoutLineItemInput>()
    private lateinit var  shoppingCartAdapter: ShoppingCartAdapter

    companion object {
        private const val TAG = "ShoppingCartFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        getShoppingCartItems()
        setListeners()
    }

    private fun setListeners() {

        binding.checkoutButton.setOnClickListener {


            Log.i(TAG, "setListeners: ${productsLine}")

            viewModel.createCheckout(productsLine)

            lifecycleScope.launch {
                viewModel.checkoutResponse.collect { result ->
                    when (result) {
                        is ApiState.Failure -> Log.e(TAG, "Checkout Failed: ${result.msg}")
                        ApiState.Loading -> Log.i(TAG, "Checkout Loading")
                        is ApiState.Success -> {
                            Log.i(TAG, "Checkout Success url: ${result.response?.checkout?.webUrl}")

                            navigateToPaymentFragment(result.response?.checkout?.webUrl ?: "")

                        }
                    }
                }
            }
        }


    }


    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory = ShoppingCartViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ShoppingCartViewModel::class.java)
    }

    private fun getShoppingCartItems() {
        viewModel.getShoppingCartItems(viewModel.readCartId())
        Log.i(TAG, "Fetching ShoppingCartItems for CartId: ${viewModel.readCartId()}")
        lifecycleScope.launch {
            viewModel.cartItems.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.e(TAG, "Failed to get items: ${result.msg}")
                    ApiState.Loading -> {
                        binding.cartShimmer.startShimmer()
                    }
                    is ApiState.Success -> {
                        Log.i(TAG, "Successfully fetched items: ${result.response.size}")
                        binding.cartShimmer.stopShimmer()
                        binding.cartShimmer.visibility = View.GONE
                        setupRecyclerView(result.response)
                        updateProductsLine(result.response)
                        getCurrentCurrency()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(items: List<CartProduct>) {
        shoppingCartAdapter = ShoppingCartAdapter(requireContext(), items.toMutableList()) { itemId ->
            val cartId = viewModel.readCartId()
            Log.i(TAG, "Removing item $itemId from cart $cartId")
            removeItemFromCart(cartId, itemId)
        }

        binding.shoppingCartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            this.adapter = shoppingCartAdapter
        }
        shoppingCartAdapter.notifyDataSetChanged()
    }

    private fun removeItemFromCart(cartId: String, lineId: String) {
        viewModel.removeProductFromCart(cartId, lineId)
        lifecycleScope.launch {
            viewModel.cartItemRemove.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.e(TAG, "Failed to remove item: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "Removing item from cart...")
                    is ApiState.Success -> getShoppingCartItems()
                }
            }
        }
    }

    private fun updateProductsLine(items: List<CartProduct>) {

        productsLine = items.map {
            CheckoutLineItemInput(quantity = it.quantity, variantId = it.variantId)
        }

    }


    private fun navigateToPaymentFragment(webUrl: String) {

        val action =
            ShoppingCartFragmentDirections.actionShoppingCartFragmentToPaymentFragment(webUrl)
        findNavController().navigate(action)

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
                            shoppingCartAdapter.updateCurrentCurrency(currency.value, currency.code)
                        }
                    }
                }
            }
        }
    }

}
