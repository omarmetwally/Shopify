package com.omarinc.shopify.network

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Input
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.omarinc.shopify.CreateCartMutation
import com.omarinc.shopify.CreateCustomerAccessTokenMutation
import com.omarinc.shopify.CreateCustomerMutation
import com.omarinc.shopify.CustomerOrdersQuery
import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.GetCollectionByHandleQuery
import com.omarinc.shopify.GetProductsByBrandIdQuery
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product

import com.omarinc.shopify.GetProductByIdQuery
import com.omarinc.shopify.GetProductsByTypeQuery
import com.omarinc.shopify.SearchProductsQuery
import com.omarinc.shopify.models.CartCreateResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.productdetails.model.Price
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.ProductImage
import com.omarinc.shopify.productdetails.model.ProductVariant
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.productdetails.model.SelectedOption
import com.omarinc.shopify.type.CustomerCreateInput
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShopifyRemoteDataSourceImpl private constructor(private val context: Context) :
    ShopifyRemoteDataSource {
    private val apolloClient: ApolloClient

    companion object {
        @Volatile
        private var instance: ShopifyRemoteDataSourceImpl? = null

        fun getInstance(context: Context): ShopifyRemoteDataSourceImpl =
            instance ?: synchronized(this) {
                instance ?: ShopifyRemoteDataSourceImpl(context).also { instance = it }
            }
    }

    init {
        apolloClient = ApolloClient.Builder()
            .serverUrl(Constants.BASE_URL_GRAPHQL)
            .addHttpHeader(Constants.ACCESS_TOKEN_KEY, Constants.ACCESS_TOKEN_VALUE)
            .build()
    }

    override fun registerUser(
        email: String,
        password: String,
        fullName: String
    ): Flow<ApiState<RegisterUserResponse>> = flow {
        val input = CustomerCreateInput(
            email = email,
            password = password,
            firstName = Optional.Present(fullName),
            lastName = Optional.Present(fullName)
        )

        val mutation = CreateCustomerMutation(input)

        try {
            emit(ApiState.Loading)
            val response = apolloClient.mutation(mutation).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.customerCreate
                if (data != null) {
                    val customer = data.customer?.let {
                        CreateCustomerMutation.Customer(
                            id = it.id,
                            email = it.email,
                            firstName = it.firstName,
                            lastName = it.lastName
                        )
                    }
                    val userErrors = data.customerUserErrors.map {
                        CreateCustomerMutation.CustomerUserError(
                            code = it.code,
                            field = it.field?.toList(),
                            message = it.message
                        )
                    }
                    emit(
                        ApiState.Success(
                            RegisterUserResponse(
                                CustomerCreateData(
                                    customer,
                                    userErrors
                                )
                            )
                        )
                    )
                } else {
                    emit(ApiState.Failure(Throwable("Response data is null")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error registering user", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun loginUser(email: String, password: String): Flow<ApiState<String>> = flow {
        val mutation = CreateCustomerAccessTokenMutation(email, password)

        try {
            emit(ApiState.Loading)
            val response = apolloClient.mutation(mutation).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.customerAccessTokenCreate
                if (data?.customerAccessToken != null) {
                    emit(ApiState.Success(data.customerAccessToken.accessToken))
                } else {
                    val errorMessage =
                        data?.customerUserErrors?.joinToString { it.message } ?: "Unknown error"
                    emit(ApiState.Failure(Throwable(errorMessage)))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error logging in", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun getBrands(): Flow<ApiState<List<Brands>>> = flow {
        val query = GetBrandsQuery()
        Log.i("TAG", "getBrands: ")
        try {
            emit(ApiState.Loading)
            val response: ApolloResponse<GetBrandsQuery.Data> = apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "getBrands: error")
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.collections?.edges
                if (data != null) {
                    Log.i("TAG", "getBrands: " + data)
                    var brands: MutableList<Brands> = mutableListOf()
                    data.forEach {
                        brands.add(Brands(it.node.id, it.node.title, it.node.image?.url.toString()))
                    }
                    emit(ApiState.Success(brands))
                } else {
                    Log.i("TAG", "getBrands: Unknown error")
                    emit(ApiState.Failure(Throwable("Unknown error")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching collections", e)
            emit(ApiState.Failure(e))
        }
    }


    override fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>> = flow {
        val query = GetProductsByBrandIdQuery(id)
        Log.i("TAG", "getProducts: before")
        try {
            emit(ApiState.Loading)

            val response: ApolloResponse<GetProductsByBrandIdQuery.Data> =
                apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "getBrands: error")
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.collection?.products
                if (data != null) {
                    Log.i("TAG", "getBrands: " + data)
                    var products: MutableList<Product> = mutableListOf()

                    data.edges.forEach {
                        products.add(
                            Product(
                                it.node.id, it.node.title, it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString()
                            ,it.node.productType.toString())
                        )
                    }
                    emit(ApiState.Success(products))
                } else {
                    Log.i("TAG", "getBrands: Unknown error")
                    emit(ApiState.Failure(Throwable("Unknown error")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching collections", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun getProductById(productId: String): Flow<ApiState<ProductDetails>> = flow {
        val query = GetProductByIdQuery(productId)

        try {
            val response = apolloClient.query(query).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                throw ApolloException(errorMessages)
            }

            val data = response.data?.product
            if (data != null) {
                val variants = data.variants.edges.map { edge ->
                    ProductVariant(
                        id = edge.node.id,
                        priceV2 = Price(
                            amount = edge.node.priceV2.amount,
                            currencyCode = edge.node.priceV2.currencyCode.toString()
                        ),
                        selectedOptions = edge.node.selectedOptions.map { option ->
                            SelectedOption(
                                name = option.name,
                                value = option.value
                            )
                        }
                    )
                }
                val productDetails = ProductDetails(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    productType = data.productType,
                    vendor = data.vendor,
                    totalInventory = data.totalInventory,
                    price = data.variants.edges.firstOrNull()?.node?.priceV2?.amount ?: "0.0",
                    images = data.images.edges.map { ProductImage(it.node.originalSrc) },
                    onlineStoreUrl = data.onlineStoreUrl,
                    variants = variants
                )
                emit(ApiState.Success(productDetails))
            } else {
                throw ApolloException("Response data is null")
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching product details", e)
            emit(ApiState.Failure(e))
        }
    }

    override suspend fun searchProducts(query: String): List<Products> {
        val response = apolloClient.query(
            SearchProductsQuery(query = query)
        ).execute()

        if (response.hasErrors()) {
            throw ApolloException(response.errors?.joinToString { it.message } ?: "Unknown error")
        }

        val products = response.data?.products?.edges?.mapNotNull { edge ->
            edge.node?.let {
                Products(
                    id = it.id,
                    title = it.title,
                    description = it.description.orEmpty(),
                    imageUrl = it.images.edges.firstOrNull()?.node?.src.toString() ?: "",
                    price = it.variants.edges.firstOrNull()?.node?.priceV2?.amount ?: "0.0"
                )
            }
        } ?: emptyList()

        return products
    }


    override fun getCutomerOrders(token: String): Flow<ApiState<List<Order>>> = flow {
        val query = CustomerOrdersQuery(token)
        Log.i("TAG", "getORders: ")
        try {
            emit(ApiState.Loading)
            val response: ApolloResponse<CustomerOrdersQuery.Data> =
                apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "get Orders: error")
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.customer?.orders?.edges
                if (data != null) {
                    Log.i("TAG", "getOrders: " + data)
                    var orders: MutableList<Order> = mutableListOf()
                    data.forEach {
                        orders.add(
                            Order(
                                it.node.id,
                                it.node.name, it.node.billingAddress?.address1 ?: "",
                                it.node.currentTotalPrice.amount as Double,
                                it.node.currentTotalPrice.currencyCode as Int,
                                it.node.currentSubtotalPrice.amount as Double,
                                it.node.currentSubtotalPrice.currencyCode as Int,
                                it.node.currentTotalTax.amount as Double,
                                it.node.currentTotalTax.currencyCode as Int,
                                it.node.canceledAt.toString()
                            )
                        )
                    }
                    emit(ApiState.Success(orders))
                } else {
                    Log.i("TAG", "getOrders: Unknown error")
                    emit(ApiState.Failure(Throwable("Unknown error")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching collections", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun getProductByType(type: String): Flow<ApiState<List<Product>>> = flow {
        val query = GetProductsByTypeQuery(type)
        Log.i("TAG", "getProducts Types: before")
        try {
            emit(ApiState.Loading)

            val response: ApolloResponse<GetProductsByTypeQuery.Data> =
                apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "getProducts Types: error")
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.products
                if (data != null) {
                    Log.i("TAG", "getTypes: " + data)
                    var products: MutableList<Product> = mutableListOf()

                    data.edges.forEach {
                        products.add(
                            Product(
                                it.node.id, it.node.title, it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString()
                            ,it.node.productType)
                        )
                    }
                    emit(ApiState.Success(products))
                } else {
                    Log.i("TAG", "getBrands: Unknown error")
                    emit(ApiState.Failure(Throwable("Unknown error")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching collections", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>> = flow {
        val query = GetCollectionByHandleQuery(handle)
        Log.i("TAG", "getCollection Types: before")
        try {
            emit(ApiState.Loading)

            val response: ApolloResponse<GetCollectionByHandleQuery.Data> =
                apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "getProducts Types: error")
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.collectionByHandle?.products
                if (data != null) {
                    Log.i("TAG", "getTypes: " + data)
                    var products: MutableList<Product> = mutableListOf()

                    data.edges.forEach {
                        products.add(
                            Product(
                                it.node.id, it.node.title, it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString()
                            ,it.node.productType)
                        )
                    }
                    val collection = Collection(
                        response.data?.collectionByHandle?.id ?: "",
                        response.data?.collectionByHandle?.title ?: "",
                        response.data?.collectionByHandle?.description ?: "", products
                    )
                    emit(ApiState.Success(collection))
                } else {
                    Log.i("TAG", "getBrands: Unknown error")
                    emit(ApiState.Failure(Throwable("Unknown error")))
                }
            }
        } catch (e: ApolloException) {
            Log.e("ShopifyRemoteDataSource", "Error fetching collections", e)
            emit(ApiState.Failure(e))
        }
    }

    override fun createCart(token: String): Flow<ApiState<String?>> = flow {
        val mutation = CreateCartMutation(token)

        try {
            emit(ApiState.Loading)

            val response = apolloClient.mutation(mutation).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                response.data?.let { data ->
                    emit(ApiState.Success(data.cartCreate?.cart?.id))
                } ?: run {
                    emit(ApiState.Failure(Throwable("Response data is null")))
                }
            }

        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }
    }

}
