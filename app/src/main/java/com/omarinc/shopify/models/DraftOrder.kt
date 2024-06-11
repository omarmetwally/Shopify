package com.omarinc.shopify.models

import com.google.gson.annotations.SerializedName

data class DraftOrderRequest(
    @SerializedName("draft_order")
    val draftOrder: DraftOrder
)

data class DraftOrderResponse(
    @SerializedName("draft_order")
    val draftOrder: DraftOrder
)

data class DraftOrder(
    @SerializedName("line_items")
    val lineItems: List<LineItem>,
    val customer: Customer,
    @SerializedName("billing_address")
    val billingAddress: Address,
    @SerializedName("shipping_address")
    val shippingAddress: Address
)

data class LineItem(
    val title: String,
    @SerializedName("variant_id")
    val variantId: Long,
    val quantity: Int,
    val price: String
)

data class Customer(
    val email: String
)

data class Address(
    val address1: String,
    val city: String,
    val province: String,
    val zip: String,
    val country: String
)
