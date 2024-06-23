package com.omarinc.shopify.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel.Companion
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ShopifyRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {


    companion object {
        const val TAG = "HomeViewModel"
    }

    private val _brandsState = MutableStateFlow<ApiState<List<Brands>>>(ApiState.Loading)
    val brandsState: StateFlow<ApiState<List<Brands>>> = _brandsState

    private val _filteredProductsState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val filteredProductsState: StateFlow<ApiState<List<Product>>> = _filteredProductsState

    private val _productsState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val productsState: StateFlow<ApiState<List<Product>>> = _productsState


    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private var _coupons = MutableStateFlow<ApiState<PriceRulesResponse>>(ApiState.Loading)
    val coupons = _coupons.asStateFlow()

    private var _couponDetails = MutableStateFlow<ApiState<DiscountCodesResponse>>(ApiState.Loading)
    val couponDetails = _couponDetails.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("EGP")
    val currencyUnit = _currencyUnit.asStateFlow()


    private val _hasCart = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    val hasCart: StateFlow<ApiState<Boolean>> = _hasCart

    private val _cartId = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartId: StateFlow<ApiState<String?>> = _cartId

    private val _customerCart = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val customerCart: StateFlow<ApiState<String?>> = _customerCart

    val maxPrice = MutableStateFlow<Int>(10000)


    fun filterProducts(products: List<Product>) {
        Log.i(TAG, "filterProducts: ")
        val filteredResults = products.filter {
            Log.i(TAG, "filterProducts: " + it)
            val price =
                (it.price as? String)?.toDoubleOrNull() ?: Double.MAX_VALUE
            price <= maxPrice.value
        }
        _filteredProductsState.value = ApiState.Success(filteredResults)
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


    fun createCart(email: String) {
        viewModelScope.launch {
            repository.createCart(email, repository.readUserToken()).collect { response ->
                _cartId.value = response
            }
        }
    }

    fun isCustomerHasCart(email: String) {
        viewModelScope.launch {
            try {
                val hasCart = firebaseRepository.isCustomerHasCart(email)
                _hasCart.value = ApiState.Success(hasCart)
            } catch (e: Exception) {
                _hasCart.value = ApiState.Failure(e)
            }
        }
    }

    fun addCustomerCart(email: String, cartId: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.addCustomerCart(email, cartId)
            } catch (e: Exception) {
                Log.e(ProductDetailsViewModel.TAG, "Error adding customer cart: ", e)
            }
        }
    }

    suspend fun readCustomerEmail(): String {

        return repository.readEmailFromSharedPreferences(USER_EMAIL)
    }


    fun getCartByCustomer(email: String) {
        viewModelScope.launch {
            _customerCart.value = ApiState.Loading
            try {
                val cartId = firebaseRepository.getCartByCustomer(email)
                _customerCart.value = ApiState.Success(cartId)
            } catch (e: Exception) {
                _customerCart.value = ApiState.Failure(e)
                Log.e(ProductDetailsViewModel.TAG, "Error getting customer cart: ", e)
            }
        }
    }


    class HomeViewModelFactory(
        private val repository: ShopifyRepository,
        private val firebaseRepository: FirebaseRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(repository, firebaseRepository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
