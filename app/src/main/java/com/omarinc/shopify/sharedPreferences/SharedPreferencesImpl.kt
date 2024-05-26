package com.omarinc.shopify.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.utilities.Constants

class SharedPreferencesImpl private constructor(context: Context) : ISharedPreferences {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(Constants.SETTINGS, AppCompatActivity.MODE_PRIVATE)
    }

    companion object {
        private var instance: SharedPreferencesImpl? = null

        fun getInstance(context: Context): SharedPreferencesImpl {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesImpl(context).also { instance = it }
            }
        }
    }

    override fun writeStringToSharedPreferences(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun readStringFromSharedPreferences(key: String): String {
        return sharedPreferences.getString(key, "null") ?: "null"
    }

    override fun writeBooleanToSharedPreferences(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun readBooleanFromSharedPreferences(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
}