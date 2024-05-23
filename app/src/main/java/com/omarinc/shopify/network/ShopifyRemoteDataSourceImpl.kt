package com.omarinc.shopify.network

import android.content.Context
import android.util.Log
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShopifyRemoteDataSourceImpl private constructor(private val context: Context) : ShopifyRemoteDataSource {
    private val shopifyService: ShopifyApiService

    companion object {
        @Volatile
        private var instance: ShopifyRemoteDataSourceImpl? = null

        fun getInstance(context: Context): ShopifyRemoteDataSourceImpl =
            instance ?: synchronized(this) {
                instance ?: ShopifyRemoteDataSourceImpl(context).also { instance = it }
            }
    }

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        shopifyService = retrofit.create(ShopifyApiService::class.java)
    }

    override fun registerUser(email: String, password: String, fullName: String): Flow<Response<RegisterUserResponse>> = flow {
        val mutation = """
        mutation {
            customerCreate(input: {
                email: "$email",
                password: "$password",
                firstName: "$fullName",
                lastName: "$fullName"
            }) {
                customer {
                    id
                    email
                    firstName
                    lastName
                }
                customerUserErrors {
                    code
                    field
                    message
                }
            }
        }
    """.trimIndent()

        Log.d("ShopifyMutation", mutation)

        val requestBody = mapOf("query" to mutation)


        try {
            val response = shopifyService.registerUser(requestBody)
            if (response.isSuccessful && response.body() != null) {
                emit(response)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ShopifyRemoteDataSource", "Error registering user: $errorBody")
                throw HttpException(response)
            }
        } catch (e: Exception) {
            Log.e("ShopifyRemoteDataSource", "Error registering user", e)
            throw e
        }
    }

}
