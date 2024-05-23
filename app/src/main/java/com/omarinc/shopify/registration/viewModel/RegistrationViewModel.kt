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
            _apiState.value = ApiState.Loading
            try {
                repository.registerUser(email, password, firstName).collect { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            if (it.data.customerCreate.customer != null) {
                                _apiState.value = ApiState.Success(it)
                            } else {
                                val errorMessage = it.data.customerCreate.customerUserErrors.joinToString { error -> error.message }
                                _apiState.value = ApiState.Failure(Throwable(errorMessage))
                            }
                        }
                    } else {
                        _apiState.value = ApiState.Failure(Throwable(response.errorBody()?.string()))
                    }
                }
            } catch (e: Exception) {
                _apiState.value = ApiState.Failure(e)
            }
        }
    }
}
