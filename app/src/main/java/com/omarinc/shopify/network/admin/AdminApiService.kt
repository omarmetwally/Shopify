package com.omarinc.shopify.network.admin

import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.utilities.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface AdminApiService {


    @Headers(
        "Content-Type: application/json",
        "X-Shopify-Access-Token: ${Constants.ADMIN_ACCESS_TOKEN}"
    )

    @GET("price_rules.json")
    suspend fun getCoupons(): Response<PriceRulesResponse>

    @Headers(
        "Content-Type: application/json",
        "X-Shopify-Access-Token: ${Constants.ADMIN_ACCESS_TOKEN}"
    )
    @GET("price_rules/{couponId}/discount_codes.json")
    suspend fun getCouponDetails(@Path("couponId") couponId: String): Response<DiscountCodesResponse>
}
