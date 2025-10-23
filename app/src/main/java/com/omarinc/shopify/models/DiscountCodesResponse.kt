package com.omarinc.shopify.models

data class DiscountCodesResponse(
    val discount_codes: List<DiscountCode>
)

data class DiscountCode(
    val id: Long,
    val price_rule_id: Long,
    val code: String,
    val usage_count: Int,
    val created_at: String,
    val updated_at: String
)

