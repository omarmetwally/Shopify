package com.omarinc.shopify.network.admin

import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.utilities.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AdminApiService {


    @Headers(
        "Content-Type: application/json",
        "X-Shopify-Access-Token: ${Constants.ADMIN_ACCESS_TOKEN}"
    )

    @GET("price_rules.json")
    suspend fun getCoupons(): Response<PriceRulesResponse>
}
