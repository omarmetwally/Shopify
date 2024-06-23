package com.omarinc.shopify.mocks

import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSource
import kotlinx.coroutines.flow.Flow

class FakeCurrencyRemoteDataSource :CurrencyRemoteDataSource {
    override fun getCurrencyRate(requiredCurrency: String): Flow<ApiState<CurrencyResponse>> {
        TODO("Not yet implemented")
    }
}