package com.omarinc.shopify.registration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.model.RegisterUserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState<RegisterUserResponse>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<RegisterUserResponse>> = _apiState

    fun registerUser(email: String, password: String, firstName: String, phoneNumber :String) {
        viewModelScope.launch {
            repository.registerUser(email, password, firstName, phoneNumber).collect { response ->
                _apiState.value = response
            }
        }
    }
}
