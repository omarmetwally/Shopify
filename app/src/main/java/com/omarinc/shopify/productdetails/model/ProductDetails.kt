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
    val onlineStoreUrl: Any?,
    val variants: List<ProductVariant>
)
data class Price(
    val amount: Any,
    val currencyCode: String
)
data class ProductVariant(
    val id: String,
    val priceV2: Price,
    val selectedOptions: List<SelectedOption>
)

data class SelectedOption(
    val name: String,
    val value: String
)
data class ProductImage(
    val src: Any
)

data class Products(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val price: Any,
    var convertedPrice: Double? = price.toString().toDouble()
)
