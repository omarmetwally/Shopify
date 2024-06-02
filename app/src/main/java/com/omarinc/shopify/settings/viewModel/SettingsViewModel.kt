package com.omarinc.shopify.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants.CURRENCY_RATE
import com.omarinc.shopify.utilities.Constants.CURRENCY_UNIT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: ShopifyRepository) : ViewModel() {


    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    fun getRequiredCurrency(requiredCurrency: Currencies) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCurrencyRate(requiredCurrency)
                .catch { error ->
                    _requiredCurrency.value = ApiState.Failure(error)
                }
                .collect { response ->
                    _requiredCurrency.value =
                        response ?: ApiState.Failure(Throwable("Something went wrong"))
                }
        }
    }

    fun setCurrency(rate: Double, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {

            repository.writeCurrencyRate(CURRENCY_RATE, rate.toLong())

            repository.writeCurrencyUnit(CURRENCY_UNIT, unit)
        }
    }
}