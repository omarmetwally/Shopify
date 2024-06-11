package com.omarinc.shopify.map.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddressViewModel(private val repository: ShopifyRepository) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
    }

    private val _address = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val address: MutableStateFlow<ApiState<String?>> = _address

    fun createAddress(
        customerAddress: CustomerAddress,
    ) {
        viewModelScope.launch {
            repository.createAddress(customerAddress, repository.readUserToken()).collect {
                Log.i(TAG, "Token:${repository.readUserToken()} ")
                _address.value = it
            }
        }

    }
}