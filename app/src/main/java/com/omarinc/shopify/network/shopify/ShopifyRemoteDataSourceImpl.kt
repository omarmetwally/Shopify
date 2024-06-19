package com.omarinc.shopify.network.shopify

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.omarinc.shopify.AddProductToCartMutation
import com.omarinc.shopify.CreateAddressMutation
import com.omarinc.shopify.CreateCartMutation
import com.omarinc.shopify.CreateCustomerAccessTokenMutation
import com.omarinc.shopify.CreateCustomerMutation
import com.omarinc.shopify.CustomerAddressesQuery
import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.CustomerOrdersQuery
import com.omarinc.shopify.DeleteAddressMutation
import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.GetCollectionByHandleQuery
import com.omarinc.shopify.GetProductsByBrandIdQuery
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product

import com.omarinc.shopify.GetProductByIdQuery
import com.omarinc.shopify.GetProductsByTypeQuery
import com.omarinc.shopify.GetProductsInCartQuery
import com.omarinc.shopify.RemoveProductFromCartMutation
import com.omarinc.shopify.SearchProductsQuery
import com.omarinc.shopify.models.AddToCartResponse
import com.omarinc.shopify.models.Cart
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.UserError
import com.omarinc.shopify.network.ApiState
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
        const val TAG = "ShopifyRemoteDataSource"

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
        fullName: String,
        phoneNumber: String
    ): Flow<ApiState<RegisterUserResponse>> = flow {
        val input = CustomerCreateInput(
            email = email,
            password = password,
            firstName = Optional.Present(fullName),
            lastName = Optional.Present(fullName),
            phone = Optional.Present(phoneNumber)
        )

        val mutation = CreateCustomerMutation(input)

        try {
            emit(ApiState.Loading)
            val response = apolloClient.mutation(mutation).execute()

            Log.e(TAG, "registerUser: $response")
            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                Log.e(TAG, "registerUser: $errorMessages")
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.customerCreate
                Log.e(TAG, "registerUser: $data")
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
                    if (customer == null) {
                        val errorMessage = userErrors.joinToString { it.message }
                        emit(ApiState.Failure(Throwable(errorMessage)))
                    } else {
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
                    }
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
                                it.node.id,
                                it.node.title,
                                it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString(),
                                it.node.productType.toString(),
                                it.node.variants.edges[0]
                                    .node.priceV2.amount.toString(),
                                it.node.variants.edges[0]
                                    .node.priceV2.currencyCode.toString(),
                            )
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


    override fun getCustomerOrders(token: String): Flow<ApiState<List<Order>>> = flow {
        val query = CustomerOrdersQuery(token)
        Log.i("TAG", "getORders: ")
        try {
            emit(ApiState.Loading)
            val response: ApolloResponse<CustomerOrdersQuery.Data> =
                apolloClient.query(query).execute()

            if (response.hasErrors()) {
                Log.i("TAG", "get Orders: error" + response.errors)
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data?.customer?.orders?.edges
                if (data != null) {
                    Log.i("TAG", "getOrders: data" + data)
                    var orders: MutableList<Order> = mutableListOf()
                    data.forEach {
                        val products = mutableListOf<Product>()
                        it.node.lineItems.edges.forEach {
                            products.add(
                                Product(
                                    it.node.variant?.id ?: "",
                                    it.node.title,
                                    it.node.variant?.product?.handle ?: "",
                                    it.node.variant?.product?.description ?: "",
                                    it.node.variant?.product?.images!!.edges[0].node.url.toString(),
                                    it.node.variant.product.productType,
                                    it.node.variant.priceV2.amount.toString(),
                                    it.node.variant.priceV2.currencyCode.toString()
                                )
                            )
                        }
                        orders.add(
                            Order(
                                it.node.id,
                                it.node.name, it.node.billingAddress?.address1,
                                it.node.currentTotalPrice.amount.toString(),
                                it.node.currentTotalPrice.currencyCode.toString(),
                                it.node.currentSubtotalPrice.amount.toString(),
                                it.node.currentSubtotalPrice.currencyCode.toString(),
                                it.node.currentTotalTax.amount.toString(),
                                it.node.currentTotalTax.currencyCode.toString(),
                                it.node.processedAt.toString(),
                                it.node.phone,
                                products
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
                                it.node.id,
                                it.node.title,
                                it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString(),
                                it.node.productType,
                                "0.0", ""
                            )
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
                                it.node.id,
                                it.node.title,
                                it.node.handle,
                                it.node.description,
                                it.node.images.edges[0].node.originalSrc.toString(),
                                it.node.productType,
                                it.node.variants.edges[0]
                                    .node.priceV2.amount.toString(),
                                it.node.variants.edges[0]
                                    .node.priceV2.currencyCode
                                    .toString()
                            ),
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

    override suspend fun createCart(email: String): Flow<ApiState<String?>> = flow {
        val mutation = CreateCartMutation(email)

        try {
            emit(ApiState.Loading)

            val response: ApolloResponse<CreateCartMutation.Data> =
                apolloClient.mutation(mutation).execute()



            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))

            } else {

                val cartId = response.data?.cartCreate?.cart?.id
                emit(ApiState.Success(cartId))

            }
        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }
    }

    override suspend fun addToCartById(
        cartId: String,
        quantity: Int,
        variantID: String
    ): Flow<ApiState<String?>> = flow {
        emit(ApiState.Loading)

        val mutation = AddProductToCartMutation(cartId, quantity, variantID)

        try {
            val response = apolloClient.mutation(mutation).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data
                if (data != null) {
                    val cartId = data.cartLinesAdd.toString()

                    emit(ApiState.Success(cartId))
                } else {
                    emit(ApiState.Failure(Throwable("Response data is null")))
                }
            }
        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }
    }

    override suspend fun removeProductFromCart(
        cartId: String,
        lineId: String
    ): Flow<ApiState<String?>> = flow {
        emit(ApiState.Loading)

        val mutation = RemoveProductFromCartMutation(cartId, lineId)

        try {
            val response: ApolloResponse<RemoveProductFromCartMutation.Data> =
                apolloClient.mutation(mutation).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val data = response.data
                if (data != null) {
                    emit(ApiState.Success(data.cartLinesRemove.toString()))
                } else {
                    emit(ApiState.Failure(Throwable("Response data is null")))
                }
            }
        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }
    }


    override suspend fun getProductsCart(cartId: String): Flow<ApiState<List<CartProduct>>> = flow {
        emit(ApiState.Loading)

        val cartProducts = mutableListOf<CartProduct>()
        val query = GetProductsInCartQuery(cartId)

        try {
            val response: ApolloResponse<GetProductsInCartQuery.Data> =
                apolloClient.query(query).execute()

            response.data?.cart?.lines?.edges?.forEach { line ->
                val node = line.node
                val merchandise = node.merchandise.onProductVariant
                if (merchandise != null) {
                    val product = merchandise.product
                    val productId = product.id
                    val productTitle = product.title ?: ""
                    val productImageUrl = product.featuredImage?.url ?: ""
                    val variantId = merchandise.id
                    val variantTitle = merchandise.title ?: ""
                    val variantPrice = merchandise.price.amount

                    cartProducts.add(
                        CartProduct(
                            id = node.id,
                            quantity = node.quantity,
                            productId = productId,
                            productTitle = productTitle,
                            productImageUrl = productImageUrl.toString(),
                            variantId = variantId,
                            variantTitle = variantTitle,
                            variantPrice = variantPrice.toString()
                        )
                    )
                }
            }

            emit(ApiState.Success(cartProducts)) // Emit success state with the list of cart products
        } catch (e: ApolloException) {
            emit(ApiState.Failure(Throwable("Error fetching products in cart: ${e.message}"))) // Emit failure state with error message
        } catch (e: Exception) {
            emit(ApiState.Failure(Throwable("An unknown error occurred: ${e.message}"))) // Emit failure state for unknown errors
        }
    }

    override suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>> =
        flow {

            emit(ApiState.Loading)

            val mutation = CreateAddressMutation(
                customerAddress.address1,
                customerAddress.address2 ?: "address",
                customerAddress.city,
                customerAddress.country,
                customerAddress.phone,
                customerAddress.firstName,
                customerAddress.lastName,
                token

            )
            try {
                val response = apolloClient.mutation(mutation).execute()


                if (response.hasErrors()) {
                    val errorMessages =
                        response.errors?.joinToString { it.message } ?: "Unknown error"
                    emit(ApiState.Failure(Throwable(errorMessages)))
                } else {

                    val addressId = response.data?.customerAddressCreate?.customerAddress?.id
                    emit(ApiState.Success(addressId))
                }

            } catch (e: ApolloException) {
                emit(ApiState.Failure(e))
            } catch (e: Exception) {
                emit(ApiState.Failure(e))
            }
        }

    override suspend fun getCustomerAddresses(token: String): Flow<ApiState<List<CustomerAddress>>> =
        flow {
            emit(ApiState.Loading)

            val query = CustomerAddressesQuery(token)
            try {
                val response = apolloClient.query(query).execute()
                if (response.hasErrors()) {
                    val errorMessages =
                        response.errors?.joinToString { it.message } ?: "Unknown error"
                    emit(ApiState.Failure(Throwable(errorMessages)))

                } else {
                    val addresses = response.data?.customer?.addresses?.edges?.map { edge ->
                        val address = edge?.node
                        CustomerAddress(
                            address?.id.toString(),
                            address?.address1.toString(),
                            address?.address2.toString(),
                            address?.city.toString(),
                            address?.country.toString(),
                            address?.phone.toString(),
                            address?.firstName.toString(),
                            address?.lastName.toString()


                        )
                    } ?: emptyList()

                    emit(ApiState.Success(addresses))
                }
            } catch (e: ApolloException) {
                emit(ApiState.Failure(e))
            }

        }

    override suspend fun deleteCustomerAddress(
        addressId: String,
        token: String
    ): Flow<ApiState<String?>> = flow {

        emit(ApiState.Loading)

        val mutation = DeleteAddressMutation(addressId, token)

        try {
            val response = apolloClient.mutation(mutation).execute()
            if (response.hasErrors()) {
                val errorMessages =
                    response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val addressId = response.data?.customerAddressDelete?.deletedCustomerAddressId
                emit(ApiState.Success(addressId))
            }

        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }

    }


    override fun getCustomerDetails(token: String): Flow<ApiState<CustomerDetailsQuery.Customer>> = flow {
        val query = CustomerDetailsQuery(token)
        try {
            emit(ApiState.Loading)
            val response = apolloClient.query(query).execute()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString { it.message } ?: "Unknown error"
                emit(ApiState.Failure(Throwable(errorMessages)))
            } else {
                val customer = response.data?.customer
                if (customer != null) {
                    emit(ApiState.Success(customer))
                } else {
                    emit(ApiState.Failure(Throwable("Customer data is null")))
                }
            }
        } catch (e: ApolloException) {
            emit(ApiState.Failure(e))
        }
    }



}
