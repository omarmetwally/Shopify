package com.omarinc.shopify.network.admin

import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.Flow

interface AdminRemoteDataSource {

    suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>>
    suspend fun getCouponDetails(couponId:String): Flow<ApiState<DiscountCodesResponse>>
}