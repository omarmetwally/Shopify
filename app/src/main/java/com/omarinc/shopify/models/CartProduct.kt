package com.omarinc.shopify.models

data class CartProduct(
    val id: String,
    val quantity: Int,
    val productId: String,
    val productTitle: String,
    val productImageUrl: String,
    val variantId: String,
    val variantTitle: String,
    val variantPrice: String
)