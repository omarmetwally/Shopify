package com.omarinc.shopify.network.currency

import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants.CURRENCY_API_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CurrencyRemoteDataSourceImpl : CurrencyRemoteDataSource {

    private val currencyService : CurrencyApiService by lazy {
        CurrencyRetrofitClient.getInstance().create(CurrencyApiService::class.java)
    }

    companion object {
        private var instance: CurrencyRemoteDataSourceImpl? = null

        fun getInstance(): CurrencyRemoteDataSourceImpl {
            if (instance == null) {
                instance = CurrencyRemoteDataSourceImpl()
            }
            return instance!!

        }
    }


    override fun getCurrencyRate(requiredCurrency: Currencies): Flow<ApiState<CurrencyResponse>> {
        return flow {
            emit(ApiState.Loading)
            try {
                val response = currencyService.getLatestRates(CURRENCY_API_KEY, "USD", requiredCurrency.name)
                if (response.isSuccessful) {
                    response.body()?.let {
                        emit(ApiState.Success(it))
                    } ?: emit(ApiState.Failure(Throwable("Response is empty")))
                } else {
                    emit(ApiState.Failure(Throwable("Response error: ${response.code()} - ${response.message()}")))
                }
            } catch (e: Exception) {
                emit(ApiState.Failure(Throwable("Network error: ${e.localizedMessage}", e)))
            }
        }
    }
}