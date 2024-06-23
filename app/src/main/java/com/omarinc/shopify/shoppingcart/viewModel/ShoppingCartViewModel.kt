package com.omarinc.shopify.shoppingcart.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.home.viewmodel.HomeViewModel.Companion.TAG
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CheckoutResponse
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.type.CheckoutLineItemInput
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ShoppingCartViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<ApiState<List<CartProduct>>>(ApiState.Loading)
    val cartItems: MutableStateFlow<ApiState<List<CartProduct>>> = _cartItems

    private val _cartItemRemove = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartItemRemove: MutableStateFlow<ApiState<String?>> = _cartItemRemove

    private val _checkoutResponse = MutableStateFlow<ApiState<CheckoutResponse?>>(ApiState.Loading)
    val checkoutResponse: MutableStateFlow<ApiState<CheckoutResponse?>> = _checkoutResponse

    private val _currencyUnit = MutableStateFlow<String>("EGP")
    val currencyUnit = _currencyUnit.asStateFlow()

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()



    fun getShoppingCartItems(cartId: String) {

        viewModelScope.launch {
            repository.getCartProducts(cartId)
                .collect {
                    _cartItems.value = it
                }
        }
    }

    fun removeProductFromCart(cartId: String, lineId: String) {
        viewModelScope.launch {
            repository.removeProductFromCart(cartId, lineId)
                .collect {
                    _cartItemRemove.value = it
                }
        }
    }

    fun readCartId(): String {
        return repository.readCartIdFromSharedPreferences()
    }

    fun createCheckout(lineItems: List<CheckoutLineItemInput>) {

        viewModelScope.launch {
            repository.createCheckout(
                lineItems,
                repository.readEmailFromSharedPreferences(USER_EMAIL)
            )
                .collect {
                    _checkoutResponse.value = it
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