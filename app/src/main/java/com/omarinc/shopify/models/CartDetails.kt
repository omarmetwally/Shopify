package com.omarinc.shopify.models

data class CartDetails(
    val cart: Carts
)

data class Carts(
    val checkoutUrl: String,
    val lines: Lines,
    val buyerIdentity: BuyerIdentity
)

data class Lines(
    val edges: List<Edge>
)

data class Edge(
    val cursor: String?
)

data class BuyerIdentity(
    val countryCode: String?,
    val deliveryAddressPreferences: List<Any>,
    val email: String,
    val phone: String?,
    val walletPreferences: List<Any>,
    val customer: CustomerCart
)

data class CustomerCart(
    val acceptsMarketing: Boolean,
    val createdAt: String,
    val displayName: String,
    val email: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val numberOfOrders: String,
    val phone: String?,
    val tags: List<String>,
    val updatedAt: String,
    val addresses: Addresses
)

data class Addresses(
    val edges: List<Edge>,
    val nodes: List<Node>
)

data class Node(
    val address1: String,
    val address2: String?,
    val city: String,
    val company: String?,
    val country: String,
    val countryCode: String,
    val countryCodeV2: String,
    val firstName: String?,
    val formatted: List<String>,
    val formattedArea: String,
    val id: String,
    val lastName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val name: String,
    val phone: String?,
    val province: String?,
    val provinceCode: String?,
    val zip: String?
)