package com.omarinc.shopify.mocks

import java.util.concurrent.ConcurrentHashMap

class FakeDatabaseReference {

    private val customerCarts = ConcurrentHashMap<String, String>()

    suspend fun addCustomerCart(email: String, cartId: String) {
        customerCarts[email] = cartId
    }

    suspend fun isCustomerHasCart(email: String): Boolean {
        return customerCarts.containsKey(email)
    }

    suspend fun getCartByCustomer(email: String): String {
        return customerCarts[email] ?: throw IllegalStateException("No cart found for the given customer")
    }
}
