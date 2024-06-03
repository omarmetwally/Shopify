package com.omarinc.shopify.favorites.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.favorites.model.FavoriteItemFirebase
import com.omarinc.shopify.favorites.model.FavoritesRepository
import com.omarinc.shopify.favorites.model.IFavoritesRepository
import com.omarinc.shopify.model.ShopifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: IFavoritesRepository) : ViewModel() {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> get() = _isFavorite

    fun checkIfFavorite(userToken: String, productId: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(userToken, productId)
        }
    }

    fun addToFavorites(userToken: String, favoriteItem: FavoriteItem) {
        viewModelScope.launch {
            repository.addFavorite(userToken, favoriteItem)
            _isFavorite.value = true
        }
    }

    fun removeFromFavorites(userToken: String, productId: String) {
        viewModelScope.launch {
            repository.removeFavorite(userToken, productId)
            _isFavorite.value = false
        }
    }

    private val _favorites = MutableStateFlow<List<FavoriteItemFirebase>>(emptyList())
    val favorites: StateFlow<List<FavoriteItemFirebase>> get() = _favorites.asStateFlow()
    fun getFavorites(userToken: String) {
        viewModelScope.launch {
            _favorites.value = repository.getFavorites(userToken)
        }
    }
}
