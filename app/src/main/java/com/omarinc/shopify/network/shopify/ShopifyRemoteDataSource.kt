package com.omarinc.shopify.network.shopify

import com.omarinc.shopify.CreateCheckoutMutation
import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CheckoutResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.type.CheckoutLineItemInput
import kotlinx.coroutines.flow.Flow

interface ShopifyRemoteDataSource {
    fun registerUser(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String
    ): Flow<ApiState<RegisterUserResponse>>

    fun loginUser(email: String, password: String): Flow<ApiState<String>>

    fun getBrands(): Flow<ApiState<List<Brands>>>

    fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>>

    fun getProductById(productId: String): Flow<ApiState<ProductDetails>>
    suspend fun searchProducts(query: String): List<Products>

    fun getCustomerOrders(token: String): Flow<ApiState<List<Order>>>

    fun getProductByType(type: String): Flow<ApiState<List<Product>>>

    fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>>

    suspend fun createCart(email: String, token: String): Flow<ApiState<String?>>

    suspend fun addToCartById(
        cartId: String,
        quantity: Int,
        variantID: String
    ): Flow<ApiState<String?>>

    suspend fun removeProductFromCart(cartId: String, lineId: String): Flow<ApiState<String?>>

    suspend fun getProductsCart(cartId: String): Flow<ApiState<List<CartProduct>>>

    suspend fun createCheckout(lineItems: List<CheckoutLineItemInput>, email: String?):Flow<ApiState<CheckoutResponse?>>
    suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>>


    suspend fun getCustomerAddresses(token: String): Flow<ApiState<List<CustomerAddress>>>

    suspend fun deleteCustomerAddress(addressId: String, token: String): Flow<ApiState<String?>>

    fun getCustomerDetails(token: String): Flow<ApiState<CustomerDetailsQuery.Customer>>

}



