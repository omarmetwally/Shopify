package com.omarinc.shopify.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.login.viewmodel.LoginViewModel
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CategoriesViewModel (private val repository: ShopifyRepository) : ViewModel() {


    companion object{
        val TAG = "CategoriesViewModel"
    }

    private val _apiState = MutableStateFlow<ApiState<List<Product>>>(ApiState.Loading)
    val apiState: StateFlow<ApiState<List<Product>>> = _apiState

    private val _collectionApiState = MutableStateFlow<ApiState<Collection>>(ApiState.Loading)
    val collectionApiState: StateFlow<ApiState<Collection>> = _collectionApiState

    private var _requiredCurrency = MutableStateFlow<ApiState<CurrencyResponse>>(ApiState.Loading)
    val requiredCurrency = _requiredCurrency.asStateFlow()

    fun getProductsByType(type:String) {
        Log.i("TAG", "getProductsByType: Viewmodel")
        viewModelScope.launch {
            _collectionApiState.collect{ result ->
                when(result){
                    is ApiState.Loading ->{

                    }

                    is ApiState.Success ->{
                       val products = result.response.products.filter {
                            it.productType.equals(type)
                        }
                        _apiState.value = ApiState.Success(products)
                    }
                    is ApiState.Failure ->{
                        Log.i("TAG", "onViewCreated: error "+result.msg)
                    }
                }
            }
           /* repository.getProductByType(type).collect {
                _apiState.value = it
            }*/
        }
    }

    fun getCollectionByHandle(handle:String) {
        Log.i("TAG", "getProductsByType: Viewmodel")
        viewModelScope.launch {
            repository.getCollectionByHandle(handle).collect {
                _collectionApiState.value = it
            }
        }
    }

    fun getRequiredCurrency() {
        Log.i(TAG, "getRequiredCurrency: ")
        viewModelScope.launch(Dispatchers.IO) {

            repository.getCurrencyRate(repository.readCurrencyUnit(Constants.CURRENCY_UNIT))
                .catch { error ->
                    _requiredCurrency.value = ApiState.Failure(error)
                }
                .collect { response ->
                    _requiredCurrency.value =
                        response ?: ApiState.Failure(Throwable("Something went wrong"))
                }
        }
    }



    class CategoriesViewModelFactory(
        private val repository: ShopifyRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
                CategoriesViewModel(repository) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
