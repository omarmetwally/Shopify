package com.omarinc.shopify.models


data class CurrencyResponse(
    val data: Map<String, Currency>,
    val meta: Meta
)

data class Currency(
    val code: String,
    val value: Double
)

data class Meta(
    val last_updated_at: String
)
