package com.omarinc.shopify.model

import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

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

    suspend fun getCurrencyRate(requiredCurrency: Currencies): Flow<ApiState<CurrencyResponse>>

    suspend fun writeCurrencyRate(key: String, value: Long)

    suspend fun writeCurrencyUnit(key: String, value: String)

    suspend fun readCurrencyRate(key: String): Long

    suspend fun readCurrencyUnit(key: String): String
    suspend fun getProductById(productId: String): Flow<ApiState<ProductDetails>>
    suspend fun searchProducts(query: String): List<Products>
}
