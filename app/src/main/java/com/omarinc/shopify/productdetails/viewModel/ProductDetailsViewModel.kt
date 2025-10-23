package com.omarinc.shopify.productdetails.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.home.viewmodel.HomeViewModel.Companion
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.CART_ID
import com.omarinc.shopify.utilities.Constants.CURRENCY_UNIT
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val repository: ShopifyRepository, private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    companion object {
        const val TAG = "ProductDetailsViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<ProductDetails>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<ProductDetails>> = _apiState

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    private val _currencyUnit = MutableStateFlow<String>("USD")
    val currencyUnit = _currencyUnit.asStateFlow()

    private val _hasCart = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    val hasCart: StateFlow<ApiState<Boolean>> = _hasCart

    private val _cartId = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartId: StateFlow<ApiState<String?>> = _cartId

    private val _customerCart = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val customerCart: StateFlow<ApiState<String?>> = _customerCart

    private val _addingToCart = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val addingToCart: StateFlow<ApiState<String?>> = _addingToCart

    fun getProductById(productId: String) {
        viewModelScope.launch {
            repository.getProductById(productId).collect { state ->
                _apiState.value = state
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

    fun createCart(email: String) {
        viewModelScope.launch {
            repository.createCart(email,repository.readUserToken()).collect { response ->
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
                Log.e(TAG, "Error adding customer cart: ", e)
            }
        }
    }


    fun getCartByCustomer(email: String) {
        viewModelScope.launch {
            _customerCart.value = ApiState.Loading
            try {
                val cartId = firebaseRepository.getCartByCustomer(email)
                _customerCart.value = ApiState.Success(cartId)
            } catch (e: Exception) {
                _customerCart.value = ApiState.Failure(e)
                Log.e(TAG, "Error getting customer cart: ", e)
            }
        }
    }

    fun addProductToCart(productId: String, quantity: Int, variantId: String) {

        viewModelScope.launch {
            repository.addToCartById(productId, quantity, variantId).collect {
                _addingToCart.value = it
            }
        }
    }

    suspend fun readCustomerEmail(): String {

        return repository.readEmailFromSharedPreferences(USER_EMAIL)
    }

    fun writeCartId(cartId: String) {
        viewModelScope.launch {
            repository.writeCartIdToSharedPreferences(cartId, CART_ID)
        }
    }
}
