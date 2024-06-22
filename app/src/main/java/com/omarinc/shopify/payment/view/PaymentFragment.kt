package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.databinding.FragmentPaymentBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.payment.viewModel.PaymentViewModel
import com.omarinc.shopify.payment.viewModel.PaymentViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import kotlinx.coroutines.launch


class PaymentFragment : BottomSheetDialogFragment() {


    private lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var webUrl: String

    companion object {
        private const val TAG = "PaymentFragment"
    }

    private val checkoutEventProcessor by lazy {
        object : DefaultCheckoutEventProcessor(requireContext()) {
            override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
                Log.i(TAG, "Checkout completed successfully.")
                clearShoppingCartItems()

            }

            override fun onCheckoutCanceled() {
                Log.i(TAG, "Checkout canceled by user.")
            }

            override fun onCheckoutFailed(error: CheckoutException) {
                Log.e(TAG, "Checkout failed with error: ${error.message}")
            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

        webUrl = arguments?.getString("webURL") ?: ""
        setListeners()

    }

    private fun setListeners() {

        binding.payWithCardButton.setOnClickListener {

            presentCheckout(convertShopifyCheckoutUrl(webUrl))


        }

        binding.cashOnDeliveryButton.setOnClickListener {


        }

    }


    private fun presentCheckout(checkoutUrl: String) {
        Log.i(TAG, "presentCheckout: ${checkoutUrl}")
        ShopifyCheckoutSheetKit.present(checkoutUrl, requireActivity(), checkoutEventProcessor)
    }

    private fun convertShopifyCheckoutUrl(originalUrl: String): String {
        val regex = Regex("""\d+/checkouts/""")
        val convertedUrl = originalUrl.replace(regex, "checkouts/co/")
        return convertedUrl
    }


    private fun setupViewModel(){

        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory = PaymentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PaymentViewModel::class.java)
    }

    private fun clearShoppingCartItems() {
        viewModel.getShoppingCartItems(viewModel.readCartId())

        lifecycleScope.launch {
            viewModel.cartItems.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.e(TAG, "Failed to get items: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "Loading ShoppingCart Items")
                    is ApiState.Success -> {
                        val items = result.response
                        for (item in items) {
                            viewModel.removeProductFromCart(viewModel.readCartId(), item.id)
                            viewModel.cartItemRemove.collect { removeResult ->
                                when (removeResult) {
                                    is ApiState.Failure -> Log.e(TAG, "Failed to remove item: ${removeResult.msg}")
                                    ApiState.Loading -> Log.i(TAG, "Removing item from cart...")
                                    is ApiState.Success -> Log.i(TAG, "Successfully removed item: ${item.id}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}