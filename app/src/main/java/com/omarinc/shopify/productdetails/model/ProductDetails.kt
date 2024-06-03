package com.omarinc.shopify.productdetails.model


data class ProductDetails(
    val id: String,
    val title: String,
    val description: String,
    val productType: String,
    val vendor: String,
    val totalInventory: Int?,
    val price: Any,
    val images: List<ProductImage>,
    val onlineStoreUrl: Any?
)


data class ProductImage(
    val src: Any
)

data class Products(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val price: Any
)
