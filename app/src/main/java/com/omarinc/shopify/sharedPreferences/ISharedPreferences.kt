package com.omarinc.shopify.sharedPreferences

interface ISharedPreferences {
    fun writeStringToSharedPreferences(key: String, value: String)
    fun readStringFromSharedPreferences(key: String): String

    fun writeBooleanToSharedPreferences(key: String, value: Boolean)
    fun readBooleanFromSharedPreferences(key: String): Boolean

    fun writeCurrencyRateToSharedPreferences(key: String, value: Long)

    fun writeCurrencyUnitToSharedPreferences(key: String, value: String)

    fun readCurrencyRateFromSharedPreferences(key: String): Long

    fun readCurrencyUnitFromSharedPreferences(key: String): String

}
