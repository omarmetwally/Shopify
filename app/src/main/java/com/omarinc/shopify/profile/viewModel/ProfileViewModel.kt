package com.omarinc.shopify.profile.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel  (private val repository: ShopifyRepository) : ViewModel() {

    private val _customerDetailsState = MutableStateFlow<ApiState<CustomerDetailsQuery.Customer>>(ApiState.Loading)
    val customerDetailsState: StateFlow<ApiState<CustomerDetailsQuery.Customer>> = _customerDetailsState

    suspend fun clearData()
    {
        repository.clearAllData()
    }
    fun getCustomerDetails() {
        viewModelScope.launch {
            val token = repository.readUserToken()
            repository.getCustomerDetails(token).collect { state ->
                _customerDetailsState.value = state
            }
        }
    }
}