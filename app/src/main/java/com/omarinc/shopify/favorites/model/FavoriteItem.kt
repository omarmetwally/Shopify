package com.omarinc.shopify.favorites.model

data class FavoriteItem(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: Any?
)



data class FavoriteItemFirebase(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: String = ""
)