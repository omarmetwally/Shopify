package com.omarinc.shopify.shoppingcart.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ShoppingCartViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<ApiState<List<CartProduct>>>(ApiState.Loading)
    val cartItems: MutableStateFlow<ApiState<List<CartProduct>>> = _cartItems

    fun getShoppingCartItems(cartId: String) {

        viewModelScope.launch {
            repository.getCartProducts(cartId)
                .collect {
                    _cartItems.value = it
                }
        }
    }


}