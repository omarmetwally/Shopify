package com.omarinc.shopify.payment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.DraftOrder
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: ShopifyRepository):ViewModel() {

    companion object {
        private const val TAG = "PaymentViewModel"
    }

    private val _cartItems = MutableStateFlow<ApiState<List<CartProduct>>>(ApiState.Loading)
    val cartItems: MutableStateFlow<ApiState<List<CartProduct>>> = _cartItems

    private val _cartItemRemove = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartItemRemove: MutableStateFlow<ApiState<String?>> = _cartItemRemove

    private val _draftOrder = MutableStateFlow<ApiState<DraftOrderResponse>>(ApiState.Loading)
    val draftOrder: MutableStateFlow<ApiState<DraftOrderResponse>> = _draftOrder

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


    fun createCashOnDeliveryOrder(draftOrder: DraftOrderRequest){
        viewModelScope.launch {
            Log.i(TAG, "createCashOnDeliveryOrder: ")
            repository.createDraftOrder(draftOrder)
        }
    }



    suspend fun readCustomerEmail(): String {

        return repository.readEmailFromSharedPreferences(USER_EMAIL)
    }
    

}