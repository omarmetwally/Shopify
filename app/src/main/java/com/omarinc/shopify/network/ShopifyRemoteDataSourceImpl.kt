package com.omarinc.shopify.network

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.omarinc.shopify.CreateCustomerAccessTokenMutation
import com.omarinc.shopify.CreateCustomerMutation
import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.GetProductsByBrandIdQuery
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brand
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product

import com.omarinc.shopify.GetProductByIdQuery
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.ProductImage
import com.omarinc.shopify.type.CustomerCreateInput
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShopifyRemoteDataSourceImpl private constructor(private val context: Context) : ShopifyRemoteDataSource {
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

    override fun registerUser(email: String, password: String, fullName: String): Flow<ApiState<RegisterUserResponse>> = flow {
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
                    emit(ApiState.Success(RegisterUserResponse(CustomerCreateData(customer, userErrors))))
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
                    val errorMessage = data?.customerUserErrors?.joinToString { it.message } ?: "Unknown error"
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
                    Log.i("TAG", "getBrands: "+data)
                    var brands:MutableList<Brands> = mutableListOf()
                    data.forEach {
                        brands.add(Brands(it.node.id,it.node.title,it.node.image?.url.toString()))
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



     override fun getProductsByBrandId(id:String): Flow<ApiState<List<Product>>> = flow {
         val query = GetProductsByBrandIdQuery(id)
         Log.i("TAG", "getProducts: before")
         try {
             emit(ApiState.Loading)

             val response: ApolloResponse<GetProductsByBrandIdQuery.Data> = apolloClient.query(query).execute()

             if (response.hasErrors()) {
                 Log.i("TAG", "getBrands: error")
                 val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                 emit(ApiState.Failure(Throwable(errorMessages)))
             } else {
                 val data = response.data?.collection?.products
                 if (data != null) {
                     Log.i("TAG", "getBrands: "+data)
                     var products:MutableList<Product> = mutableListOf()

                     data.edges.forEach {
                         products.add(Product(it.node.id,it.node.title,it.node.handle,
                             it.node.description,
                             it.node.images.edges[0].node.originalSrc.toString()))
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
                val productDetails = ProductDetails(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    productType = data.productType,
                    vendor = data.vendor,
                    totalInventory = data.totalInventory,
                    price = data.variants.edges.firstOrNull()?.node?.priceV2?.amount ?: "0.0",
                    images = data.images.edges.map { ProductImage(it.node.originalSrc) },
                    onlineStoreUrl = data.onlineStoreUrl
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
}
