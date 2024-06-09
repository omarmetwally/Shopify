package com.omarinc.shopify.network.admin

import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AdminRemoteDataSourceImpl : AdminRemoteDataSource {

    private val adminApiService: AdminApiService by lazy {
        AdminRetrofitClient.getInstance().create(AdminApiService::class.java)
    }


    companion object {
        private var instance: AdminRemoteDataSourceImpl? = null
        fun getInstance(): AdminRemoteDataSourceImpl {
            if (instance == null) {
                instance = AdminRemoteDataSourceImpl()
            }
            return instance!!
        }
    }

    override suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>> = flow {

        emit(ApiState.Loading)

        try {
            val response = adminApiService.getCoupons()
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
