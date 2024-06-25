package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.shopify.databinding.FragmentPaymentWebViewBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.payment.viewModel.PaymentViewModel
import com.omarinc.shopify.payment.viewModel.PaymentViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class PaymentWebViewFragment : Fragment() {

    companion object {
        private const val TAG = "PaymentWebViewFragment"
    }

    private var _binding: FragmentPaymentWebViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PaymentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        val paymentUrl = arguments?.getString("webUrl") ?: ""
        Log.i(TAG, "onViewCreated: ${paymentUrl}")
        setupWebView(paymentUrl)
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

    private fun setupWebView(url: String) {
        binding.paymentWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                Log.i(TAG, "Loading URL: $url")
                onPaymentSuccess()

                if (url.endsWith("thank-you")) {
                    onPaymentSuccess()
                } else if (url.contains("payment_failed") || url.contains("success=false")) {
                    onPaymentFailure()
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        val webSettings: WebSettings = binding.paymentWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        binding.paymentWebView.loadUrl(url)
    }

    private fun onPaymentSuccess() {
        Log.i(TAG, "Payment succeeded")
        clearShoppingCartItems()
        Toast.makeText(requireContext(), "Payment Success", Toast.LENGTH_LONG)

    }

    private fun onPaymentFailure() {
        Log.i(TAG, "Payment failed")
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

}

