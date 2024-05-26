package com.omarinc.shopify.network.currency

import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.Flow

interface CurrencyRemoteDataSource {

    fun getCurrencyRate(requiredCurrency: Currencies): Flow<ApiState<CurrencyResponse>>
}