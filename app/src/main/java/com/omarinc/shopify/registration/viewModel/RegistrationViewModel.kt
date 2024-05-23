package com.omarinc.shopify.registration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState>(ApiState.Loading)
    val apiState: StateFlow<ApiState> = _apiState

    fun registerUser(email: String, password: String, firstName: String) {
        viewModelScope.launch {
            repository.registerUser(email, password, firstName).collect { response ->
                if (response.customerCreate.customer != null) {
                    _apiState.value = ApiState.Success(response)
                } else {
                    val errorMessage = response.customerCreate.customerUserErrors.joinToString { it.message }
                    _apiState.value = ApiState.Failure(Throwable(errorMessage))
                }
            }
        }
    }
}
