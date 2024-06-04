package com.omarinc.shopify.models

data class Collection(
    val id: String, val title: String, val description: String,
    val products: List<Product>
)