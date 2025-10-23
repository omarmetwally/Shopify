package com.omarinc.shopify.models

data class Product(
    val id: String, val title: String, val handle: String,
    val description: String, val imageUrl: String, val productType: String,
    val price: String, val currencyCode: String,var convertedPrice: Double? = price.toDouble()
)