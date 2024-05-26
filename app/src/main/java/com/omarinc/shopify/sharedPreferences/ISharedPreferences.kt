package com.omarinc.shopify.sharedPreferences

interface ISharedPreferences {
    fun writeStringToSharedPreferences(key: String, value: String)
    fun readStringFromSharedPreferences(key: String): String

    fun writeBooleanToSharedPreferences(key: String, value: Boolean)
    fun readBooleanFromSharedPreferences(key: String): Boolean
}
