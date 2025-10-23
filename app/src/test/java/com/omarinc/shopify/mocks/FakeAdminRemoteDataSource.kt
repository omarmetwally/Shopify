package com.omarinc.shopify.mocks

import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSource
import kotlinx.coroutines.flow.Flow

class FakeAdminRemoteDataSource : AdminRemoteDataSource {
    override suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCouponDetails(couponId: String): Flow<ApiState<DiscountCodesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun completeDraftOrder(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendInvoice(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }
}