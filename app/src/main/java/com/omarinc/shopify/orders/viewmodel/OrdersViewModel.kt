package com.omarinc.shopify.orders.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.home.viewmodel.HomeViewModel.Companion.TAG
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
class OrdersViewModel (private val repository: ShopifyRepository) : ViewModel() {


    private val _apiState = MutableStateFlow<ApiState<List<Order>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Order>>> = _apiState

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("USD")
    val currencyUnit = _currencyUnit.asStateFlow()

    fun getCustomerOrders(token:String){
        viewModelScope.launch {
            repository.getCustomerOrders(token).collect{
                _apiState.value = it
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

    class OrdersViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
                OrdersViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
