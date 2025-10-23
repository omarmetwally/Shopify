package com.omarinc.shopify.network.admin

import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.Flow

interface AdminRemoteDataSource {

    suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>>

    suspend fun getCouponDetails(couponId:String): Flow<ApiState<DiscountCodesResponse>>

    suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>>

    suspend fun completeDraftOrder(orderId:Long): Flow<ApiState<DraftOrderResponse>>

    suspend fun sendInvoice(orderId:Long): Flow<ApiState<DraftOrderResponse>>


}