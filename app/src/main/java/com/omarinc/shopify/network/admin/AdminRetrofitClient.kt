package com.omarinc.shopify.network.admin

import com.omarinc.shopify.utilities.Constants.ADMIN_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AdminRetrofitClient {

    fun getInstance(): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(ADMIN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        return retrofit
    }
}