package com.omarinc.shopify.mocks

import com.omarinc.shopify.CreateCustomerMutation
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSource
import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class FakeShopifyRemoteDataSource : ShopifyRemoteDataSource {

    var shouldReturnError = false

    override fun registerUser(email: String, password: String, fullName: String, phoneNumber: String): Flow<ApiState<RegisterUserResponse>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val customer = CreateCustomerMutation.Customer(
                id = "123",
                email = email,
                firstName = fullName,
                lastName = fullName
            )
            val userErrors = emptyList<CreateCustomerMutation.CustomerUserError>()
            val data = RegisterUserResponse(CustomerCreateData(customer, userErrors))
            emit(ApiState.Success(data))
        }
    }

    override fun loginUser(email: String, password: String): Flow<ApiState<String>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            emit(ApiState.Success("fake_token"))
        }
    }

    override fun getBrands(): Flow<ApiState<List<Brands>>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val brands = listOf(Brands("1", "Brand1", "url1"), Brands("2", "Brand2", "url2"))
            emit(ApiState.Success(brands))
        }
    }

    override fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val products = listOf(Product("1", "Product1", "handle1", "desc1", "image1", "type1", "10.0", "USD"))
            emit(ApiState.Success(products))
        }
    }

    override fun getProductById(productId: String): Flow<ApiState<ProductDetails>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val productDetails = ProductDetails("1", "Product1", "desc1", "type1", "vendor1", 10, "10.0", emptyList(), "url1", emptyList())
            emit(ApiState.Success(productDetails))
        }
    }

    override suspend fun searchProducts(query: String): List<Products> {
        return if (shouldReturnError) {
            throw Throwable("Fake error")
        } else {
            listOf(Products("1", "Product1", "desc1", "image1", "10.0"))
        }
    }

    override fun getCutomerOrders(token: String): Flow<ApiState<List<Order>>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val orders = emptyList<Order>()
            emit(ApiState.Success(orders))
        }
    }

    override fun getProductByType(type: String): Flow<ApiState<List<Product>>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val products = listOf(Product("1", "Product1", "handle1", "desc1", "image1", "type1", "10.0", "USD"))
            emit(ApiState.Success(products))
        }
    }

    override fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val collection = Collection("1", "Collection1", "desc1", emptyList())
            emit(ApiState.Success(collection))
        }
    }

    override suspend fun createCart(email: String): Flow<ApiState<String?>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            emit(ApiState.Success("fake_cart_id"))
        }
    }

    override suspend fun addToCartById(cartId: String, quantity: Int, variantID: String): Flow<ApiState<String?>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            emit(ApiState.Success("fake_cart_id"))
        }
    }

    override suspend fun getProductsCart(cartId: String): Flow<ApiState<List<CartProduct>>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            val cartProducts = listOf(CartProduct("1", 1, "1", "Product1", "image1", "1", "variant1", "10.0"))
            emit(ApiState.Success(cartProducts))
        }
    }

    override suspend fun createAddress(customerAddress: CustomerAddress, token: String): Flow<ApiState<String?>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {
            emit(ApiState.Success("fake_address_id"))
        }
    }

    override fun getCustomerDetails(token: String): Flow<ApiState<CustomerDetailsQuery.Customer>> = flow {
        if (shouldReturnError) {
            emit(ApiState.Failure(Throwable("Fake error")))
        } else {

            val customer = CustomerDetailsQuery.Customer(
                id = "1",
                firstName = "omar",
                lastName = "omar",
                acceptsMarketing = true,
                email = "omar@example.com",
                phone = "+01126513889",

            )
            emit(ApiState.Success(customer))
        }
    }
}