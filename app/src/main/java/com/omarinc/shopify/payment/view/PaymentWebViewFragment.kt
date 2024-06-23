package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentPaymentWebViewBinding

class PaymentWebViewFragment : Fragment() {

    companion object {
        private const val TAG = "PaymentWebViewFragment"
    }

    private var _binding: FragmentPaymentWebViewBinding? = null
    private val binding get() = _binding!!

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


        val paymentUrl = arguments?.getString("webUrl") ?: ""
        Log.i(TAG, "onViewCreated: ${paymentUrl}")
        setupWebView(paymentUrl)
    }

    private fun setupWebView(url: String) {
        binding.paymentWebView.webViewClient = WebViewClient()
        binding.paymentWebView.webChromeClient = WebChromeClient()

        val webSettings: WebSettings = binding.paymentWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        binding.paymentWebView.loadUrl(url)
    }



    private fun convertShopifyCheckoutUrl(originalUrl: String): String {
        val pattern = Regex("""https://[^/]+/(\d+)/checkouts/([a-f0-9]+)\?key=[a-f0-9]+""")
        val matchResult = pattern.matchEntire(originalUrl)

        return if (matchResult != null) {
            val (domain, checkoutId) = matchResult.destructured
            "https://$domain/checkouts/co/$checkoutId"
        } else {
            throw IllegalArgumentException("Invalid Shopify checkout URL format")
        }
    }

}

