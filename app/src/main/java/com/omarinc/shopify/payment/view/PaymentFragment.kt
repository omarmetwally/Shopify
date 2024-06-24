package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apollographql.apollo3.api.Optional
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentPaymentBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.Address
import com.omarinc.shopify.models.Customer
import com.omarinc.shopify.models.CustomerAddress
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
import com.omarinc.shopify.type.MailingAddressInput
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class PaymentFragment : BottomSheetDialogFragment() {


    private lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var checkoutId: String
    private lateinit var defaultAddress: CustomerAddress


    companion object {
        private const val TAG = "PaymentFragment"
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
        getCustomerAddresses()
        checkoutId = arguments?.getString("checkoutId") ?: ""
        Log.i(TAG, "onViewCreated: ${checkoutId}")
        setListeners()

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

    private fun setListeners() {


        binding.payNow.setOnClickListener {

            binding.paymentMethodRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.cash_on_delivery_radio_button -> {
                        createCashOnDeliveryOrder()

                    }

                    R.id.card_radio_button -> {
                        payWithCard()
                    }
                }
            }

        }

        binding.addressCard.setOnClickListener {

            val action = PaymentFragmentDirections.actionPaymentFragmentToDefaultAddressFragment()
            findNavController().navigate(action)
        }

        binding.paymentMethodRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.cash_on_delivery_radio_button -> {
                    binding.voucherLayout.visibility = View.VISIBLE

                }

                R.id.card_radio_button -> {
                    binding.voucherLayout.visibility = View.GONE
                }
            }
        }


        binding.applyVoucherButton.setOnClickListener {
            val voucherCode = binding.voucherEditText.text.toString().trim()
            if (voucherCode.isNotEmpty()) {
                applyVoucher(voucherCode)
            } else {
                Toast.makeText(requireContext(), "Please enter a voucher code", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun applyVoucher(voucherCode: String) {
        // TODO: Add logic to apply the voucher code
        // Example: Check the voucher code and apply discount
        if (isValidVoucher(voucherCode)) {
            Toast.makeText(requireContext(), "Voucher applied successfully", Toast.LENGTH_SHORT)
                .show()
            // Apply discount logic here
        } else {
            Toast.makeText(requireContext(), "Invalid voucher code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidVoucher(voucherCode: String): Boolean {
        // TODO: Replace this with your actual voucher validation logic
        // For example, check against a list of valid voucher codes
        val validVouchers = listOf("DISCOUNT10", "SALE20", "SAVE30")
        return voucherCode in validVouchers
    }

    private fun payWithCard() {
        lifecycleScope.launch {
            viewModel.applyShippingAddress(
                checkoutId,
                MailingAddressInput(
                    address1 = Optional.present(defaultAddress?.address1),
                    city = Optional.present(defaultAddress?.city),
                    country = Optional.present("Egypt"),
                    lastName = Optional.present(defaultAddress?.lastName),
                    phone = Optional.present("01555774530"),
                    province = Optional.present("Cairo"),
                    zip = Optional.present("123")
                )
            )

            viewModel.webUrl.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "setListeners: Failure ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "setListeners: Loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "setListeners: ${result.response}")
                        val action =
                            PaymentFragmentDirections.actionPaymentFragmentToPaymentWebViewFragment(
                                result.response
                            )
                        findNavController().navigate(action)
                    }
                }

            }
        }
    }

    private fun createCashOnDeliveryOrder() {

        lifecycleScope.launch {

            viewModel.getShoppingCartItems(viewModel.readCartId())

            viewModel.cartItems.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.e(TAG, "Failed to get items: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "Loading ShoppingCart Items")
                    is ApiState.Success -> {
                        val cartProducts = result.response


                        val lineItems = cartProducts.map { cartProduct ->
                            val variantIdLong = extractProductVariantId(cartProduct.variantId) ?: 0L
                            LineItem(
                                title = cartProduct.productTitle,
                                variantId = variantIdLong,
                                quantity = cartProduct.quantity,
                                price = cartProduct.variantPrice
                            )
                        }



                        Log.i(TAG, "Email: ${viewModel.readCustomerEmail()}")
                        val draftOrder = DraftOrder(
                            id = 0,
                            lineItems = lineItems,
                            customer = Customer(email = viewModel.readCustomerEmail()),
                            billingAddress = Address(
                                address1 = defaultAddress.address1,
                                city = defaultAddress.city,
                                province = defaultAddress.city,
                                zip = "123",
                                country = "Egypt"
                            ),
                            shippingAddress = Address(
                                address1 = defaultAddress.address1,
                                city = defaultAddress.city,
                                province = defaultAddress.city,
                                zip = "123",
                                country = "Egypt"
                            )
                        )

                        val draftOrderRequest = DraftOrderRequest(draftOrder = draftOrder)

                        viewModel.createCashOnDeliveryOrder(draftOrderRequest)


                        viewModel.draftOrder.collect { draftOrderResult ->
                            when (draftOrderResult) {
                                is ApiState.Failure -> Log.e(
                                    TAG,
                                    "createCashOnDeliveryOrder: ${draftOrderResult.msg}"
                                )

                                ApiState.Loading -> Log.i(TAG, "createCashOnDeliveryOrder: Loading")
                                is ApiState.Success -> {
                                    Log.i(TAG, "createCashOnDeliveryOrder: Success")
                                    completeCashOnDeliveryOrder(draftOrderResult.response.draftOrder.id)
                                    clearShoppingCartItems()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun completeCashOnDeliveryOrder(orderId: Long) {

        viewModel.completeCashOnDeliveryOrder(orderId)

        lifecycleScope.launch {


            viewModel.completeOrder.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "completeCashOnDeliveryOrder: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "completeCashOnDeliveryOrder: Loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "completeCashOnDeliveryOrder: Sucess")
                        sendInvoice(result.response.draftOrder.id)
                    }
                }


            }


        }

    }

    private fun sendInvoice(orderId: Long) {

        viewModel.sendInvoice(orderId)

        lifecycleScope.launch {

            viewModel.emailInvoice.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "sendInvoice: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "sendInvoice: Loading")
                    is ApiState.Success -> {

                        Log.i(TAG, "sendInvoice:  Success ${result.response}")

                    }
                }

            }

        }
    }


    private fun getCustomerAddresses() {

        viewModel.getCustomersAddresses()

        lifecycleScope.launch {
            viewModel.addressList.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "getCustomerAddresses: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "getCustomerAddresses: loading")
                    is ApiState.Success -> {
                        defaultAddress = result.response?.get(0)!!
                        updateDefaultAddressUI()
                    }
                }

            }
        }

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

    private fun extractProductVariantId(variantId: String): Long? {
        // Regex pattern to extract the numeric part
        val regex = Regex("""\d+""")
        val matchResult = regex.find(variantId)
        return matchResult?.value?.toLongOrNull()
    }


    private fun updateDefaultAddressUI() {

        binding.cityAddress.text = defaultAddress?.city
        binding.detailsAddress.text = defaultAddress?.address1
    }

}


