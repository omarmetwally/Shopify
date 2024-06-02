package com.omarinc.shopify.registration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.model.RegisterUserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(private val repository: ShopifyRepository) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState<RegisterUserResponse>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<RegisterUserResponse>> = _apiState

    fun registerUser(email: String, password: String, firstName: String) {
        viewModelScope.launch {
            repository.registerUser(email, password, firstName).collect { response ->
                _apiState.value = response
            }
        }
    }
}
