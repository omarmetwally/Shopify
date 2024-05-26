package com.omarinc.shopify.model

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface ShopifyRepository {
    suspend fun registerUser(email: String, password: String, firstName: String): Flow<RegisterUserResponse>
}
