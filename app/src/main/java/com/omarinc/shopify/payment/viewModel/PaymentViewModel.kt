package com.omarinc.shopify.payment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.DraftOrder
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.type.MailingAddressInput
import com.omarinc.shopify.utilities.Constants.USER_EMAIL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: ShopifyRepository) : ViewModel() {

    companion object {
        private const val TAG = "PaymentViewModel"
    }

    private val _cartItems = MutableStateFlow<ApiState<List<CartProduct>>>(ApiState.Loading)
    val cartItems: StateFlow<ApiState<List<CartProduct>>> = _cartItems

    private val _cartItemRemove = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val cartItemRemove: StateFlow<ApiState<String?>> = _cartItemRemove

    private val _draftOrder = MutableStateFlow<ApiState<DraftOrderResponse>>(ApiState.Loading)
    val draftOrder: StateFlow<ApiState<DraftOrderResponse>> = _draftOrder

    private val _webUrl = MutableStateFlow<ApiState<String>>(ApiState.Loading)
    val webUrl: StateFlow<ApiState<String>> = _webUrl

    private val _addressList = MutableStateFlow<ApiState<List<CustomerAddress>?>>(ApiState.Loading)
    val addressList: StateFlow<ApiState<List<CustomerAddress>?>> = _addressList

    private val _completeOrder = MutableStateFlow<ApiState<DraftOrderResponse>>(ApiState.Loading)
    val completeOrder: StateFlow<ApiState<DraftOrderResponse>> = _completeOrder

    private val _emailInvoice = MutableStateFlow<ApiState<DraftOrderResponse>>(ApiState.Loading)
    val emailInvoice: StateFlow<ApiState<DraftOrderResponse>> = _emailInvoice

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


    fun createCashOnDeliveryOrder(draftOrder: DraftOrderRequest) {
        viewModelScope.launch {

            repository.createDraftOrder(draftOrder).collect {
                _draftOrder.value = it
            }
        }
    }


    suspend fun readCustomerEmail(): String {

        return repository.readEmailFromSharedPreferences(USER_EMAIL)
    }

    suspend fun applyShippingAddress(checkoutId: String, shippingAddress: MailingAddressInput) {

        viewModelScope.launch {
            repository.applyShippingAddress(checkoutId, shippingAddress)
                .collect {
                    _webUrl.value = it as ApiState<String>
                }
        }

    }

    fun getCustomersAddresses() {

        viewModelScope.launch {

            repository.getCustomerAddresses(repository.readUserToken()).collect {
                _addressList.value = it
            }

        }

    }

    fun completeDraftOrder(orderId: Long) {

        viewModelScope.launch {
            repository.completeDraftOrder(orderId).collect {
                _completeOrder.value = it
            }
        }
    }

    fun sendInvoice(orderId: Long) {

        viewModelScope.launch {
            repository.sendInvoice(orderId).collect {
                _emailInvoice.value = it
            }
        }
    }

}