package com.omarinc.shopify.network

import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartCreateResponse
import com.omarinc.shopify.models.CartLineInput
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import kotlinx.coroutines.flow.Flow

interface ShopifyRemoteDataSource {
    fun registerUser(
        email: String,
        password: String,
        fullName: String
    ): Flow<ApiState<RegisterUserResponse>>

    fun loginUser(email: String, password: String): Flow<ApiState<String>>

    fun getBrands(): Flow<ApiState<List<Brands>>>

    fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>>

    fun getProductById(productId: String): Flow<ApiState<ProductDetails>>
    suspend fun searchProducts(query: String): List<Products>

    fun getCutomerOrders(token: String): Flow<ApiState<List<Order>>>

    fun getProductByType(type: String): Flow<ApiState<List<Product>>>

    fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>>

    suspend fun createCart(email: String): Flow<ApiState<String?>>

    suspend fun addToCart(
        cartId: String,
        lines: List<CartLineInput>
    ): Flow<ApiState<CartCreateResponse>>

    suspend fun getProductsCart(cartId: String): Flow<ApiState<List<CartProduct>>>

    suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>>

}
