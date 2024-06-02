package com.omarinc.shopify.splashscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.utilities.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private val repository: ShopifyRepository) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Checking)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState

    fun checkUserState() {
        viewModelScope.launch {
            val userToken = repository.readUserToken()
            val skipPressed = repository.readBooleanFromSharedPreferences(Constants.USER_SKIPPED)
            if (userToken != "null" || skipPressed) {
                _navigationState.value = SplashNavigationState.NavigateToMain
            } else {
                _navigationState.value = SplashNavigationState.NavigateToLogin
            }
        }
    }
}

sealed class SplashNavigationState {
    object Checking : SplashNavigationState()
    object NavigateToMain : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
}
