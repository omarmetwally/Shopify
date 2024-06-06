package com.omarinc.shopify.models

data class Order(
    val id:String,
    val name:String,val address : String, val totalPriceAmount:String,
    val totalPriceCurrencyCode:String,
                 val subTotalPriceAmount:String,
                 val subTotalPriceCurrencyCode:String,
                 val totalTaxAmount:String,
                 val totalTaxCurrencyCode:String,
    val canceledAt:String,)