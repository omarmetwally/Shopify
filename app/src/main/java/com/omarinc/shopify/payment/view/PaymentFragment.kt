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
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class PaymentFragment : BottomSheetDialogFragment() {


    private lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: PaymentViewModel
    private lateinit var checkoutId: String
    private lateinit var defaultAddress: CustomerAddress
    private var totalPrice: Double = 0.0

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
        checkoutId = arguments?.getString("checkoutId") ?: ""
        totalPrice = arguments?.getString("totalPrice")?.toDouble() ?: 0.0
        val selectedAddress = arguments?.getSerializable("selectedAddress") as? CustomerAddress
        if (selectedAddress != null) {
            defaultAddress = selectedAddress
            updateDefaultAddressUI()
        } else {
            getCustomerAddresses()
        }
        Log.i(TAG, "onViewCreated: ${totalPrice}")
        setListeners()
        setDefaultRadioButton()

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

    private fun setDefaultRadioButton() {

        binding.paymentMethodRadioGroup.check(R.id.cash_on_delivery_radio_button)

        binding.voucherLayout.visibility = View.VISIBLE
    }

    private fun setListeners() {
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


        binding.payNow.setOnClickListener {
            when (binding.paymentMethodRadioGroup.checkedRadioButtonId) {
                R.id.cash_on_delivery_radio_button -> {
                    createCashOnDeliveryOrder()
                    clearShoppingCartItems()
                    dismiss()
                }

                R.id.card_radio_button -> {
                    payWithCard()

                }
            }
        }


        binding.addressCard.setOnClickListener {
            val action = PaymentFragmentDirections.actionPaymentFragmentToDefaultAddressFragment(
                checkoutId,
                totalPrice.toString()
            )
            findNavController().navigate(action)
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
        val discountPercentage = getVoucherDiscount(voucherCode)
        if (discountPercentage > 0) {
            Toast.makeText(
                requireContext(),
                "Voucher applied successfully: $discountPercentage% off",
                Toast.LENGTH_SHORT
            ).show()
            binding.voucherSuccessTextView.visibility = View.VISIBLE
            binding.voucherFailTextView.visibility = View.GONE
            applyDiscount(discountPercentage)
        } else {
            binding.voucherSuccessTextView.visibility = View.GONE
            binding.voucherFailTextView.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Invalid voucher code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVoucherDiscount(voucherCode: String): Int {
        return when (voucherCode) {
            "MADAND-SV#10" -> 10
            "MADAND-SV#20" -> 20
            else -> 0
        }
    }

    private fun applyDiscount(discountPercentage: Int) {
        val originalAmount =
            getOriginalOrderAmount()
        val discountedAmount = originalAmount * (1 - discountPercentage / 100.0)
        updateOrderAmount(discountedAmount)
    }


    private fun getOriginalOrderAmount(): Double {

        return totalPrice
    }

    private fun updateOrderAmount(amount: Double) {
        binding.orderAmountTextView.text = String.format("%.2f", amount)
    }

    private fun payWithCard() {
        lifecycleScope.launch {
            viewModel.applyShippingAddress(
                checkoutId,
                MailingAddressInput(
                    address1 = Optional.present(defaultAddress?.address1),
                    city = Optional.present(defaultAddress?.city),
                    country = Optional.present("Egypt"),
                    firstName = Optional.present(defaultAddress?.firstName),
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
                        dismiss()
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
                            email = viewModel.readCustomerEmail(),
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

        binding.addressCustomerName.text = "${defaultAddress.firstName} ${defaultAddress.lastName}"
        binding.detailsAddress.text = "${defaultAddress.city}, ${defaultAddress.address1}"
    }


    override fun onStop() {
        super.onStop()

        dismiss()

    }

}


