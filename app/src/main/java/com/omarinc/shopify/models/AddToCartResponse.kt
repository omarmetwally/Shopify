package com.omarinc.shopify.models


data class AddToCartResponse(
    val cart: Cart?,
    val userErrors: List<UserError>
)
data class Cart(
    val id: String
)

data class UserError(
    val field: List<String>?,
    val message: String
)