package com.omarinc.shopify.search.viewmodel

import androidx.lifecycle.ViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.productdetails.model.Products
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SearchViewModel (private val repository: ShopifyRepository) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<Products>>(emptyList())
    val searchResults: StateFlow<List<Products>> get() = _searchResults
    init {
        // Initial loading isA
    }
    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchProducts(query)
                _searchResults.value = response
            } catch (e: Exception) {
            }
        }
    }
}