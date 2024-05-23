package com.omarinc.shopify.network

import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.utilities.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ShopifyApiService {
    @Headers(
        "Content-Type: application/json",
        "X-Shopify-Storefront-Access-Token: ${Constants.ACCESS_TOKEN}"
    )
    @POST("graphql.json")
    suspend fun registerUser(@Body request: Map<String, String>): Response<RegisterUserResponse>
}
