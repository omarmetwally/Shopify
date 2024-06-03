package com.omarinc.shopify.models

data class Order(
    val id:String,
    val name:String,val address : String, val totalPriceAmount:Double,
    val totalPriceCurrencyCode:Int,
                 val subTotalPriceAmount:Double,
                 val subTotalPriceCurrencyCode:Int,
                 val totalTaxAmount:Double,
                 val totalTaxCurrencyCode:Int,
    val canceledAt:String,)