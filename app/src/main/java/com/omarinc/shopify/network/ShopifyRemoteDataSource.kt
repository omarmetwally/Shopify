package com.omarinc.shopify.network

import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brand
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartCreateResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

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

    fun getCollectionByHandle(handle:String) : Flow<ApiState<Collection>>

    fun createCart(email: String): Flow<ApiState<String?>>
}
