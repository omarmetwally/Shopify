package com.omarinc.shopify.favorites.model

data class FavoriteItem(
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImage: Any
)
