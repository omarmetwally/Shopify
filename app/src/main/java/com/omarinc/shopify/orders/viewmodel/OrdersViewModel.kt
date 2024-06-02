package com.omarinc.shopify.orders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class OrdersViewModel @Inject constructor(private val repository: ShopifyRepository) : ViewModel() {


    private val _apiState = MutableStateFlow<ApiState<List<Order>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Order>>> = _apiState

    fun getCutomerOrders(token:String){
        viewModelScope.launch {
            repository.getCutomerOrders(token).collect{
                _apiState.value = it
            }
        }
    }
}