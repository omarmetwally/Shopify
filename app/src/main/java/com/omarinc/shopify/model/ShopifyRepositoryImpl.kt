package com.omarinc.shopify.model

import com.omarinc.shopify.network.ShopifyRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response

class ShopifyRepositoryImpl(
    private val shopifyRemoteDataSource: ShopifyRemoteDataSource
) : ShopifyRepository {

    companion object {

        @Volatile
        private var instance: ShopifyRepositoryImpl? = null

        fun getInstance(
            shopifyRemoteDataSource: ShopifyRemoteDataSource
        ): ShopifyRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: ShopifyRepositoryImpl(shopifyRemoteDataSource).also { instance = it }
            }
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        firstName: String
    ): Flow<RegisterUserResponse> {
        return withContext(Dispatchers.IO) {
            shopifyRemoteDataSource.registerUser(email, password, firstName)
        }
    }
}
