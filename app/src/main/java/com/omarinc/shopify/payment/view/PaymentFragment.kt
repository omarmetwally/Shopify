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
import com.omarinc.shopify.models.Address
import com.omarinc.shopify.models.Customer
import com.omarinc.shopify.models.DraftOrder
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.LineItem
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
            //"https://mad44-sv-and.myshopify.com/cart/c/Z2NwLWV1cm9wZS13ZXN0MTowMUoxMTJHRDFOUDZHVkVYSkQySjBBU1g4Uw?key=e856db46ee3cc0f93d60436f380f9fd3"


        }

        binding.cashOnDeliveryButton.setOnClickListener {

            testDraftOrder()

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


    private fun setupViewModel() {

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
                            removeItemCompletely(item.id, item.quantity)
                        }
                    }
                }
            }
        }
    }

    private fun removeItemCompletely(itemId: String, quantity: Int) {
        lifecycleScope.launch {
            repeat(quantity) {
                viewModel.removeProductFromCart(viewModel.readCartId(), itemId)
                viewModel.cartItemRemove.collect { removeResult ->
                    when (removeResult) {
                        is ApiState.Failure -> Log.e(
                            TAG,
                            "Failed to remove item: ${removeResult.msg}"
                        )

                        ApiState.Loading -> Log.i(TAG, "Removing item from cart...")
                        is ApiState.Success -> Log.i(TAG, "Successfully removed item: $itemId")
                    }
                }
            }
        }
    }




    fun extractProductVariantId(variantId: String): Long? {
        // Regex pattern to extract the numeric part
        val regex = Regex("""\d+""")
        val matchResult = regex.find(variantId)
        return matchResult?.value?.toLongOrNull()
    }


    fun testDraftOrder() {

        val billingAddress = Address(
            address1 = "123 Main St",
            city = "Anytown",
            province = "Anystate",
            zip = "12345",
            country = "USA"
        )

        val shippingAddress = Address(
            address1 = "123 Main St",
            city = "Anytown",
            province = "Anystate",
            zip = "12345",
            country = "USA"
        )

        val customer = Customer(
            email = "customer@example.com"
        )

        val lineItem = LineItem(
            title = "Sample Product",
            variantId = 43633467621555L,
            quantity = 1,
            price = "19.99"
        )

        val lineItems = listOf(lineItem)

        val draftOrder = DraftOrder(
            lineItems = lineItems,
            customer = customer,
            billingAddress = billingAddress,
            shippingAddress = shippingAddress
        )

        val draftOrderRequest = DraftOrderRequest(
            draftOrder = draftOrder
        )

        viewModel.createCashOnDeliveryOrder(draftOrderRequest)

        lifecycleScope.launch {

            viewModel.draftOrder.collect { result ->

                when(result){
                    is ApiState.Failure -> Log.i(TAG, "testDraftOrder: Failure ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "testDraftOrder: Loading")
                    is ApiState.Success -> {

                        Log.i(TAG, "testDraftOrder: Success")
                    }
                }

            }
        }

    }
}


