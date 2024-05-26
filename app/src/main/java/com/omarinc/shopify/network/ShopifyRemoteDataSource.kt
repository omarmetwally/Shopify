package com.omarinc.shopify.network

import com.omarinc.shopify.model.RegisterUserResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface ShopifyRemoteDataSource {
    fun registerUser(email: String, password: String, fullName: String): Flow<RegisterUserResponse>
}
