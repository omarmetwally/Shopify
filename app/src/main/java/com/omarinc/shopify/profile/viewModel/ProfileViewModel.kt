package com.omarinc.shopify.profile.viewModel

import androidx.lifecycle.ViewModel
import com.omarinc.shopify.model.ShopifyRepository

class ProfileViewModel  (private val repository: ShopifyRepository) : ViewModel() {

    suspend fun clearData()
    {
        repository.clearAllData()
    }
}