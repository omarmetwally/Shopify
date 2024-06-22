package com.omarinc.shopify.payment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: ShopifyRepository):ViewModel() {

    private val _cartItemRemove = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartItemRemove: MutableStateFlow<ApiState<String?>> = _cartItemRemove


    fun removeProductFromCart(cartId: String, lineId: String) {
        viewModelScope.launch {
            repository.removeProductFromCart(cartId, lineId)
                .collect {
                    _cartItemRemove.value = it
                }
        }
    }

}