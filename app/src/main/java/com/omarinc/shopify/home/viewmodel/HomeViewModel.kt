package com.omarinc.shopify.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.DiscountCodesResponse
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

    private val _brandsState = MutableStateFlow<ApiState<List<Brands>>>(ApiState.Loading)
    val brandsState: StateFlow<ApiState<List<Brands>>> = _brandsState

    private val _productsState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val productsState: StateFlow<ApiState<List<Product>>> = _productsState


    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private var _coupons = MutableStateFlow<ApiState<PriceRulesResponse>>(ApiState.Loading)
    val coupons = _coupons.asStateFlow()

    private var _couponDetails = MutableStateFlow<ApiState<DiscountCodesResponse>>(ApiState.Loading)
    val couponDetails = _couponDetails.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("USD")
    val currencyUnit = _currencyUnit.asStateFlow()

    val maxPrice = MutableStateFlow<Int>(10000)

    fun filterProducts(products: List<Product>) {
        Log.i(TAG, "filterProducts: ")
        val filteredResults = products.filter {
            Log.i(TAG, "filterProducts: "+it)
            val price =
                (it.price as? String)?.toDoubleOrNull() ?: Double.MAX_VALUE
            price <= maxPrice.value
        }
        _productsState.value = ApiState.Success(filteredResults)
    }


    fun getBrands() {
        Log.i("TAG", "getBrands: Viewmodel")
        viewModelScope.launch {
            repository.getBrands().collect {
                _brandsState.value = it
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
                _productsState.value = it
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
    fun getCoupons() {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getCoupons().collect {

                _coupons.value = it
            }
        }
    }
    fun writeIsFirstTimeUser(key: String, value: Boolean) {
        viewModelScope.launch {
            repository.writeIsFirstTimeUser(key, value)
        }
    }

    suspend fun readIsFirstTimeUser(key: String): Boolean {
        return repository.readIsFirstTimeUser(key)

    }

    fun getCouponDetails(couponId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCouponDetails(couponId).collect {
                _couponDetails.value = it
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
