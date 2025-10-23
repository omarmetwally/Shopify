package com.omarinc.shopify.mocks

import com.omarinc.shopify.sharedPreferences.ISharedPreferences

class FakeSharedPreferences :ISharedPreferences {
    override fun writeStringToSharedPreferences(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun readStringFromSharedPreferences(key: String): String {
        TODO("Not yet implemented")
    }

    override fun writeBooleanToSharedPreferences(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun readBooleanFromSharedPreferences(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeCurrencyRateToSharedPreferences(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    override fun writeCurrencyUnitToSharedPreferences(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun readCurrencyRateFromSharedPreferences(key: String): Long {
        TODO("Not yet implemented")
    }

    override fun readCurrencyUnitFromSharedPreferences(key: String): String {
        TODO("Not yet implemented")
    }

    override fun clearAllData() {
        TODO("Not yet implemented")
    }
}