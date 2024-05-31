package com.omarinc.shopify.productdetails.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModel(private val repository: ShopifyRepository) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState<ProductDetails>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<ProductDetails>> = _apiState

    fun getProductById(productId: String) {
        viewModelScope.launch {
            repository.getProductById(productId).collect { state ->
                _apiState.value = state
            }
        }
    }
}