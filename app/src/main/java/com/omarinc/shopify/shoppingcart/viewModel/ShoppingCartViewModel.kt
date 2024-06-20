package com.omarinc.shopify.shoppingcart.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CheckoutResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.type.CheckoutLineItemInput
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ShoppingCartViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<ApiState<List<CartProduct>>>(ApiState.Loading)
    val cartItems: MutableStateFlow<ApiState<List<CartProduct>>> = _cartItems

    private val _cartItemRemove = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartItemRemove: MutableStateFlow<ApiState<String?>> = _cartItemRemove

    private val _paymentUrl = MutableStateFlow<ApiState<CheckoutResponse?>>(ApiState.Loading)
    val paymentUrl: MutableStateFlow<ApiState<CheckoutResponse?>> = _paymentUrl
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
                    _paymentUrl.value = it
                }

        }
    }

}