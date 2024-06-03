package com.omarinc.shopify.network.currency

import com.omarinc.shopify.models.CurrencyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {


    @GET("v3/latest")
    suspend fun getLatestRates(
        @Query("apikey") apiKey: String,
        @Query("base_currency") baseCurrency: String,
        @Query("currencies") currencies: String
    ): Response<CurrencyResponse>

}