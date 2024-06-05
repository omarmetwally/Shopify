package com.omarinc.shopify.productdetails.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.utilities.Constants.CURRENCY_UNIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val repository: ShopifyRepository, private val favouritesRepository: FirebaseRepository
) : ViewModel() {

    companion object {
        const val TAG = "ProductDetailsViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<ProductDetails>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<ProductDetails>> = _apiState

    private val _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency: StateFlow<ApiState<CurrencyResponse>> = _requiredCurrency

    private val _hasCart = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    val hasCart: StateFlow<ApiState<Boolean>> = _hasCart

    private val _cartId = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartId: StateFlow<ApiState<String?>> = _cartId

    fun getProductById(productId: String) {
        viewModelScope.launch {
            repository.getProductById(productId).collect { state ->
                _apiState.value = state
            }
        }
    }

    fun getRequiredCurrency() {
        Log.i(TAG, "getRequiredCurrency: ")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCurrencyRate(repository.readCurrencyUnit(CURRENCY_UNIT))
                .catch { error ->
                    _requiredCurrency.value = ApiState.Failure(error)
                }
                .collect { response ->
                    _requiredCurrency.value =
                        response ?: ApiState.Failure(Throwable("Something went wrong"))
                }
        }
    }

    fun createCart(token: String) {
        viewModelScope.launch {
            repository.createCart(token).collect { response ->
                _cartId.value = response
            }
        }
    }

    fun isCustomerHasCart(email: String) {
        viewModelScope.launch {
            try {
                val hasCart = favouritesRepository.isCustomerHasCart(email)
                _hasCart.value = ApiState.Success(hasCart)
            } catch (e: Exception) {
                _hasCart.value = ApiState.Failure(e)
            }
        }
    }

    fun addCustomerCart(email: String, cartId: String) {
        viewModelScope.launch {
            try {
                favouritesRepository.addCustomerCart(email, cartId)
                // You might want to update some state here if needed
            } catch (e: Exception) {
                // Handle the error appropriately, e.g., log it or update a state flow
                Log.e(TAG, "Error adding customer cart: ", e)
            }
        }
    }
}
