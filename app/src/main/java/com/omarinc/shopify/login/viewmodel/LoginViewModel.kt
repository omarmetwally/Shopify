package com.omarinc.shopify.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState<String>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<String>> = _apiState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            repository.loginUser(email, password).collect { response ->
                _apiState.value = response
            }
        }
    }

    private val _skipButtonState = MutableStateFlow<Boolean>(false)
    val skipButtonState: StateFlow<Boolean> = _skipButtonState
    suspend fun onSkipButtonPressed() {
        repository.writeBooleanToSharedPreferences(Constants.USER_SKIPPED, true)
        _skipButtonState.value = true
    }
}
