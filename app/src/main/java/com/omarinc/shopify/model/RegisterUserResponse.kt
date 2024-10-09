package com.omarinc.shopify.model

import com.omarinc.shopify.CreateCustomerMutation

data class RegisterUserResponse(
    val customerCreate: CustomerCreateData
)

data class CustomerCreateData(
    val customer: CreateCustomerMutation.Customer?,
    val customerUserErrors: List<CreateCustomerMutation.CustomerUserError>
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
