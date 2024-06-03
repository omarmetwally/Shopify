package com.omarinc.shopify.orders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class OrdersViewModel (private val repository: ShopifyRepository) : ViewModel() {


    private val _apiState = MutableStateFlow<ApiState<List<Order>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Order>>> = _apiState

    fun getCutomerOrders(token:String){
        viewModelScope.launch {
            repository.getCutomerOrders(token).collect{
                _apiState.value = it
            }
        }
    }

    class OrdersViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
                OrdersViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
