package com.omarinc.shopify.models

import com.omarinc.shopify.type.CheckoutLineItemInput

data class CreateCheckoutMutation(
    val lineItems: List<CheckoutLineItemInput>,
    val email: String,
    val buyerIdentity: BuyerIdentity
) {
    data class BuyerIdentity(
        val email: String
    )
}

