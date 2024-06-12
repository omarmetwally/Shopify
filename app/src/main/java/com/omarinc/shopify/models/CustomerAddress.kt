package com.omarinc.shopify.models

data class CustomerAddress(
    val id: String,
    var address1: String,
    val address2: String?,
    var city: String,
    var country: String,
    var phone: String,
    var firstName: String,
    var lastName: String
)
