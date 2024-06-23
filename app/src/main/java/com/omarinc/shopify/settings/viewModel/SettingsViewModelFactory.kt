package com.omarinc.shopify.settings.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omarinc.shopify.model.ShopifyRepository

class SettingsViewModelFactory(private val repository: ShopifyRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            SettingsViewModel(repository) as T


        }else{

            throw IllegalArgumentException("View model class not found")
        }


    }
}