package com.omarinc.shopify.categories.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: ShopifyRepository) : ViewModel() {


    val maxPrice = MutableStateFlow<Int>(10000)


    companion object {
        val TAG = "CategoriesViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Product>>> = _apiState

    private val _collectionApiState = MutableStateFlow<ApiState<Collection>>(ApiState.Loading)
    val collectionApiState: StateFlow<ApiState<Collection>> = _collectionApiState

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("EGP")
    val currencyUnit = _currencyUnit.asStateFlow()

    fun filterProducts(products: List<Product>) {
        val filteredResults = products.filter {
            val price =
                (it.price as? String)?.toDoubleOrNull() ?: Double.MAX_VALUE
            price <= maxPrice.value
        }
        _apiState.value = ApiState.Success(filteredResults)
    }
    fun getProductsByType(type: String) {
        viewModelScope.launch {
            _collectionApiState.collect { result ->
                when (result) {
                    is ApiState.Loading -> {

                    }

                    is ApiState.Success -> {
                        val products = result.response.products.filter {
                            it.productType.equals(type)
                        }
                        _apiState.value = ApiState.Success(products)
                    }

                    is ApiState.Failure -> {
                        Log.i("TAG", "onViewCreated: error " + result.msg)
                    }
                }
            }
        }
    }
    fun getCollectionByHandle(handle: String) {
        Log.i("TAG", "getProductsByType: Viewmodel")
        viewModelScope.launch {
            repository.getCollectionByHandle(handle).collect {
                _collectionApiState.value = it
            }
        }
    }

    fun getCurrencyUnit() {

        viewModelScope.launch(Dispatchers.IO) {
            _currencyUnit.value = repository.readCurrencyUnit(Constants.CURRENCY_UNIT)
        }
    }
    fun getRequiredCurrency() {
        Log.i(HomeViewModel.TAG, "getRequiredCurrency: ")
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
    class CategoriesViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
                CategoriesViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
