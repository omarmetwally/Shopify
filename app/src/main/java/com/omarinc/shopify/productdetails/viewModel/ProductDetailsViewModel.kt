package com.omarinc.shopify.productdetails.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.utilities.Constants.CURRENCY_UNIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProductDetailsViewModel(private val repository: ShopifyRepository) : ViewModel() {


    companion object {
        val TAG = "ProductDetailsViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<ProductDetails>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<ProductDetails>> = _apiState

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

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

    private val _cartId = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartId: StateFlow<ApiState<String?>> = _cartId

    fun createCart(token: String){

        viewModelScope.launch {
            repository.createCart(token).collect { response ->
                _cartId.value = response
            }
        }
    }




}