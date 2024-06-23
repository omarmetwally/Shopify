package com.omarinc.shopify.network.admin

import android.util.Log
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.log

class AdminRemoteDataSourceImpl : AdminRemoteDataSource {


    private val adminApiService: AdminApiService by lazy {
        AdminRetrofitClient.getInstance().create(AdminApiService::class.java)
    }


    companion object {

        private const val TAG = "AdminRemoteDataSourceImpl"
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

    override suspend fun getCouponDetails(couponId: String): Flow<ApiState<DiscountCodesResponse>> =
        flow {

            emit(ApiState.Loading)


            try {
                val response = adminApiService.getCouponDetails(couponId)
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

    override suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>> = flow {
        Log.i(TAG, "createDraftOrder: remote")
        emit(ApiState.Loading)
        try {
            val response = adminApiService.createDraftOrder(draftOrder)

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.i(TAG, "createDraftOrder: $it")
                    emit(ApiState.Success(it))
                } ?: emit(ApiState.Failure(Throwable("Response is empty")))
            } else {
                Log.i(TAG, "createDraftOrder: ${response.body()}")
                emit(ApiState.Failure(Throwable("Response error: ${response.code()} - ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.i(TAG, "createDraftOrder: ${e.message}")
            emit(ApiState.Failure(Throwable("Network error: ${e.localizedMessage}", e)))
        }
    }

    override suspend fun completeDraftOrder(orderId: Long): Flow<ApiState<DraftOrderResponse>> =
        flow {

            emit(ApiState.Loading)


            try {
                val response = adminApiService.completeDraftOrder(orderId)
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

    override suspend fun sendInvoice(orderId: Long): Flow<ApiState<DraftOrderResponse>> =
        flow {

            emit(ApiState.Loading)


            try {
                val response = adminApiService.sendInvoice(orderId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.i("TAG", "sendInvoice: $it")
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
