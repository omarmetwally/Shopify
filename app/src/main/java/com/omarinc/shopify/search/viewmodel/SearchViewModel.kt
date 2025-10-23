package com.omarinc.shopify.search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.productdetails.model.Products
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.home.viewmodel.HomeViewModel.Companion.TAG
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: ShopifyRepository) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<Products>>(emptyList())
    val searchResults: StateFlow<List<Products>> get() = _searchResults

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("USD")
    val currencyUnit = _currencyUnit.asStateFlow()

    init {
        // Initial loading isA
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchProducts(query)
                _searchResults.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun getCurrencyUnit() {

        viewModelScope.launch(Dispatchers.IO) {
            _currencyUnit.value = repository.readCurrencyUnit(Constants.CURRENCY_UNIT)
        }
    }


    fun getRequiredCurrency() {
        Log.i(TAG, "getRequiredCurrency: ")
        viewModelScope.launch(Dispatchers.IO) {

            repository.getCurrencyRate(repository.readCurrencyUnit(Constants.CURRENCY_UNIT))
                .catch { error ->
                    _requiredCurrency.value = ApiState.Failure(error)
                }
                .collect { response ->
                    _requiredCurrency.value =
                        response ?: ApiState.Failure(Throwable("Something went wrong"))
                }
        }
    }

}