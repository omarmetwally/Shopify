package com.omarinc.shopify.models

import com.omarinc.shopify.CustomerOrdersQuery

data class CheckoutResponse(
    val checkout: Checkout?,

)

data class Checkout(
    val id: String,
    val webUrl: String,
    val lineItems: List<LineItemCheckout>
)

data class Variant(
    val id: String,
    val title: String,
    val price: CustomerOrdersQuery.PriceV2
)

data class LineItemCheckout(
    val title: String,
    val quantity: Int,
    val variant: Variant,
    val price: Double
)

