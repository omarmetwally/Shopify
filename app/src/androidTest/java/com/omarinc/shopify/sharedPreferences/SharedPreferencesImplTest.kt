package com.omarinc.shopify.sharedPreferences


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omarinc.shopify.mocks.FakeSharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferencesImplTest {

    private lateinit var sharedPreferences: SharedPreferencesImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        sharedPreferences = SharedPreferencesImpl.getInstance(context)
    }

    @Test
    fun writeStringToSharedPreferences_and_readStringFromSharedPreferences() {
        val key = "test_key"
        val value = "test_value"
        sharedPreferences.writeStringToSharedPreferences(key, value)
        val result = sharedPreferences.readStringFromSharedPreferences(key)
        assertEquals(value, result)
    }

    @Test
    fun writeBooleanToSharedPreferences_and_readBooleanFromSharedPreferences() {
        val key = "test_boolean_key"
        val value = true
        sharedPreferences.writeBooleanToSharedPreferences(key, value)
        val result = sharedPreferences.readBooleanFromSharedPreferences(key)
        assertEquals(value, result)
    }

    @Test
    fun writeCurrencyRateToSharedPreferences_and_readCurrencyRateFromSharedPreferences() {
        val key = "test_currency_rate_key"
        val value = 123456L
        sharedPreferences.writeCurrencyRateToSharedPreferences(key, value)
        val result = sharedPreferences.readCurrencyRateFromSharedPreferences(key)
        assertEquals(value, result)
    }

    @Test
    fun writeCurrencyUnitToSharedPreferences_and_readCurrencyUnitFromSharedPreferences() {
        val key = "test_currency_unit_key"
        val value = "USD"
        sharedPreferences.writeCurrencyUnitToSharedPreferences(key, value)
        val result = sharedPreferences.readCurrencyUnitFromSharedPreferences(key)
        assertEquals(value, result)
    }

    @Test
    fun clearAllData() {
        sharedPreferences.writeStringToSharedPreferences("key1", "value1")
        sharedPreferences.writeBooleanToSharedPreferences("key2", true)
        sharedPreferences.writeCurrencyRateToSharedPreferences("key3", 123456L)
        sharedPreferences.writeCurrencyUnitToSharedPreferences("key4", "USD")

        sharedPreferences.clearAllData()

        assertEquals("null", sharedPreferences.readStringFromSharedPreferences("key1"))
        assertFalse(sharedPreferences.readBooleanFromSharedPreferences("key2"))
        assertEquals(0L, sharedPreferences.readCurrencyRateFromSharedPreferences("key3"))
        assertEquals("null", sharedPreferences.readCurrencyUnitFromSharedPreferences("key4"))
    }
}
