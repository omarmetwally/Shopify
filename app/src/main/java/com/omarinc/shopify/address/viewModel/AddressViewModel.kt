package com.omarinc.shopify.address.viewModel

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

    private val _addressList = MutableStateFlow<ApiState<List<CustomerAddress>?>>(ApiState.Loading)
    val addressList: MutableStateFlow<ApiState<List<CustomerAddress>?>> = _addressList

    private val _addressDelete = MutableStateFlow<ApiState<String?>>(ApiState.Loading)
    val addressDelete: MutableStateFlow<ApiState<String?>> = _addressDelete
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

    fun getCustomersAddresses() {

        viewModelScope.launch {

            repository.getCustomerAddresses(repository.readUserToken()).collect {
                _addressList.value = it
            }

        }

    }

    fun deleteAddress(addressId: String) {

        viewModelScope.launch {
            repository.deleteCustomerAddress(addressId, repository.readUserToken()).collect {
                _addressDelete.value = it
            }
        }

    }
}