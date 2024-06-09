package com.omarinc.shopify.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.login.viewmodel.LoginViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ShopifyRepository) : ViewModel() {


    companion object {
        const val TAG = "HomeViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<List<Brands>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Brands>>> = _apiState

    private val _productsApiState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val productsApiState: StateFlow<ApiState<List<Product>>> = _productsApiState


    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private var _coupons = MutableStateFlow<ApiState<PriceRulesResponse>>(ApiState.Loading)
    val coupons = _coupons.asStateFlow()

    fun getBrands() {
        Log.i("TAG", "getBrands: Viewmodel")
        viewModelScope.launch {
            repository.getBrands().collect {
                _apiState.value = it
            }
        }
    }

    private val _searchResults = MutableStateFlow<List<Products>>(emptyList())
    val searchResults: StateFlow<List<Products>> get() = _searchResults
    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchProducts(query)
                _searchResults.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun getProductsByBrandId(id: String) {
        Log.i("TAG", "getBrands: Viewmodel")
        viewModelScope.launch {
            repository.getProductsByBrandId(id).collect {
                _productsApiState.value = it
            }
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

    fun getCoupons() {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getCoupons().collect {

                _coupons.value = it
            }
        }
    }


    class HomeViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
