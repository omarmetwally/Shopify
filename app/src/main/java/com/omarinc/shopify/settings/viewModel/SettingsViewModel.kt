package com.omarinc.shopify.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants.CURRENCY_RATE
import com.omarinc.shopify.utilities.Constants.CURRENCY_UNIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val repository: ShopifyRepository) : ViewModel() {


    suspend fun getCurrencyUnit(): String {
        val currencyUnit = viewModelScope.async(Dispatchers.IO) {
            repository.readCurrencyUnit(CURRENCY_UNIT)
        }

        return currencyUnit.await()
    }

    fun setCurrency(unit: String) {
        viewModelScope.launch(Dispatchers.IO) {

            repository.writeCurrencyUnit(CURRENCY_UNIT, unit)
        }
    }


}