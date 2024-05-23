package com.omarinc.shopify.model

data class RegisterUserResponse(
    val data: CustomerCreateContainer
)

data class CustomerCreateContainer(
    val customerCreate: CustomerCreateData
)

data class CustomerCreateData(
    val customer: Customer?,
    val customerUserErrors: List<CustomerUserError>
)

data class Customer(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class CustomerUserError(
    val code: String,
    val field: List<String>?,
    val message: String
)
