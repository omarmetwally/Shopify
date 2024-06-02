package com.omarinc.shopify.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.login.viewmodel.LoginViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: ShopifyRepository) : ViewModel() {


    private val _apiState = MutableStateFlow<ApiState<List<Brands>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Brands>>> = _apiState

    private val _productsApiState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val productsApiState: StateFlow<ApiState<List<Product>>> = _productsApiState

    fun getBrands() {
        Log.i("TAG", "getBrands: Viewmodel")
        viewModelScope.launch {
            repository.getBrands().collect {
                _apiState.value = it
            }
        }
    }

    fun getProductsByBrandId(id: String) {
        Log.i("TAG", "getBrands: Viewmodel")
        viewModelScope.launch {
            repository.getProductsByBrandId(id).collect {
                _productsApiState.value = it
            }
        }
    }


    class HomeViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                HomeViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
