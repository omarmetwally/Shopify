package com.omarinc.shopify.shoppingcart.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent

class CheckoutEventProcessor(private val context: Context) : DefaultCheckoutEventProcessor(context) {

    companion object {
       private const val TAG = "CheckoutEventProcessor"
    }

    override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
        Log.i(TAG, "Checkout completed successfully.")
    }

    override fun onCheckoutCanceled() {
        Log.i(TAG, "Checkout canceled by user.")
    }

    override fun onCheckoutFailed(error: CheckoutException) {
        Log.e(TAG, "Checkout failed with error: ${error.message}")
    }

    override fun onCheckoutLinkClicked(uri: Uri) {
        Log.i(TAG, "Checkout link clicked: $uri")
        handleUri(uri)
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        Log.i(TAG, "Web pixel event: $event")
    }

    private fun handleUri(uri: Uri) {
        val intent = when (uri.scheme) {
            "mailto" -> Intent(Intent.ACTION_SENDTO, uri)
            "tel" -> Intent(Intent.ACTION_DIAL, uri)
            "http", "https" -> Intent(Intent.ACTION_VIEW, uri)
            else -> null
        }

        if (intent != null) {
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle URI: ${uri.toString()}", e)
                Toast.makeText(context, "Cannot handle this link", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Unsupported URI scheme: ${uri.scheme}")
            handleCustomScheme(uri)
        }
    }

    private fun handleCustomScheme(uri: Uri) {
        // Example: Handling a custom scheme, e.g., "myapp://"
        // Implement your custom scheme handling logic here

        when (uri.scheme) {
            "myapp" -> {
                // Handle the custom scheme
                Log.i(TAG, "Handling custom scheme: ${uri.scheme}")
                // You can start an activity, show a dialog, etc.
                // For demonstration, let's just log the URI
                Toast.makeText(context, "Custom scheme detected: ${uri.toString()}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e(TAG, "Unknown URI scheme: ${uri.scheme}")
                Toast.makeText(context, "Unsupported link type: ${uri.scheme}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
