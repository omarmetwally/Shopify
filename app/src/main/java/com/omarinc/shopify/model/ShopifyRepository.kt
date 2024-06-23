package com.omarinc.shopify.model

import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CheckoutResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.type.CheckoutLineItemInput
import kotlinx.coroutines.flow.Flow

interface ShopifyRepository {
    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        phoneNumber: String
    ): Flow<ApiState<RegisterUserResponse>>

    fun getBrands(): Flow<ApiState<List<Brands>>>
    fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>>


    suspend fun loginUser(email: String, password: String): Flow<ApiState<String>>

    suspend fun writeBooleanToSharedPreferences(key: String, value: Boolean)
    suspend fun readBooleanFromSharedPreferences(key: String): Boolean
    suspend fun readUserToken(): String

    suspend fun getCurrencyRate(requiredCurrency: String): Flow<ApiState<CurrencyResponse>>

    suspend fun writeCurrencyRate(key: String, value: Long)

    suspend fun writeCurrencyUnit(key: String, value: String)

    suspend fun readCurrencyRate(key: String): Long

    suspend fun readCurrencyUnit(key: String): String
    suspend fun getProductById(productId: String): Flow<ApiState<ProductDetails>>
    suspend fun searchProducts(query: String): List<Products>

    fun getCustomerOrders(token: String): Flow<ApiState<List<Order>>>

    fun getProductByType(type: String): Flow<ApiState<List<Product>>>

    fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>>

    suspend fun createCart(token: String): Flow<ApiState<String?>>

    suspend fun readEmailFromSharedPreferences(key: String): String

    suspend fun addToCartById(
        cartId: String,
        quantity: Int,
        variantID: String
    ): Flow<ApiState<String?>>

    suspend fun removeProductFromCart(cartId: String, lineId: String): Flow<ApiState<String?>>

    suspend fun getCartProducts(cartId: String): Flow<ApiState<List<CartProduct>>>

    suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>>

    suspend fun writeCartIdToSharedPreferences(key: String, value: String)
    fun readCartIdFromSharedPreferences(): String

    suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>>

    suspend fun getCouponDetails(couponId: String): Flow<ApiState<DiscountCodesResponse>>

    suspend fun getCustomerAddresses(token: String): Flow<ApiState<List<CustomerAddress>>>

    suspend fun deleteCustomerAddress(addressId: String, token: String): Flow<ApiState<String?>>

    suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>>

    suspend fun completeDraftOrder(orderId: Long): Flow<ApiState<DraftOrderResponse>>

    suspend fun sendInvoice(orderId: Long): Flow<ApiState<DraftOrderResponse>>

    suspend fun writeIsFirstTimeUser(key: String, value: Boolean)

    suspend fun readIsFirstTimeUser(key: String): Boolean

    suspend fun clearAllData()

    fun getCustomerDetails(token: String): Flow<ApiState<CustomerDetailsQuery.Customer>>

    suspend fun createCheckout(
        lineItems: List<CheckoutLineItemInput>, email: String?
    ): Flow<ApiState<CheckoutResponse?>>
}
