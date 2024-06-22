package com.omarinc.shopify.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.databinding.FragmentPaymentBinding
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent


class PaymentFragment : BottomSheetDialogFragment() {


    private lateinit var binding: FragmentPaymentBinding

    private lateinit var webUrl: String

    companion object {
        private const val TAG = "PaymentFragment"
    }

    private val checkoutEventProcessor by lazy {
        object : DefaultCheckoutEventProcessor(requireContext()) {
            override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
                Log.i(TAG, "Checkout completed successfully.")
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


}