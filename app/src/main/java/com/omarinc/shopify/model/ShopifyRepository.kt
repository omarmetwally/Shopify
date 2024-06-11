package com.omarinc.shopify.model

import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow

interface ShopifyRepository {
    suspend fun registerUser(
        email: String,
        password: String,
        firstName: String
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

    fun getCutomerOrders(token: String): Flow<ApiState<List<Order>>>

    fun getProductByType(type: String): Flow<ApiState<List<Product>>>

    fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>>

    suspend fun createCart(token: String): Flow<ApiState<String?>>

    suspend fun readEmailFromSharedPreferences(key: String): String

    suspend fun addToCartById(
        cartId: String,
        quantity: Int,
        variantID: String
    ): Flow<ApiState<String?>>

    suspend fun getCartProducts(cartId: String): Flow<ApiState<List<CartProduct>>>

    suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>>

    suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>>
}
