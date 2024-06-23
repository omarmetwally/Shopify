package com.omarinc.shopify.payment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentPaymentWebViewBinding

class PaymentWebViewFragment : Fragment() {

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
        Log.i(TAG, "onViewCreated: ${convertShopifyCheckoutUrl(paymentUrl)}")
        setupWebView(convertShopifyCheckoutUrl(paymentUrl))
    }

    private fun setupWebView(url: String) {
        binding.paymentWebView.webViewClient = WebViewClient()
        binding.paymentWebView.loadUrl(url)
    }

    companion object {
        private const val TAG = "PaymentWebViewFragment"
        fun newInstance(webUrl: String) = PaymentWebViewFragment().apply {
            arguments = Bundle().apply {
                putString("webUrl", webUrl)
            }
        }
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

