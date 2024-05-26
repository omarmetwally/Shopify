package com.omarinc.shopify.network

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import com.omarinc.shopify.CreateCustomerAccessTokenMutation
import com.omarinc.shopify.CreateCustomerMutation
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.model.RegisterUserResponse
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
}
