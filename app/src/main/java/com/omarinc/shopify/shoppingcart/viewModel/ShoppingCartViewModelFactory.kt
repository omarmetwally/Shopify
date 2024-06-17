package com.omarinc.shopify.shoppingcart.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omarinc.shopify.model.ShopifyRepository

class ShoppingCartViewModelFactory(
    private val repository: ShopifyRepository,
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingCartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingCartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}