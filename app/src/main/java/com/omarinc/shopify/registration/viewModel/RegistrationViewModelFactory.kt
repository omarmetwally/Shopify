package com.omarinc.shopify.registration.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.registration.viewmodel.RegistrationViewModel

class RegistrationViewModelFactory(
    private val repository: ShopifyRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel Class not found")
    }
}
