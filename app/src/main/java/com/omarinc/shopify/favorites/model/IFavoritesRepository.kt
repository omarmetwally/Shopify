package com.omarinc.shopify.favorites.model

interface IFavoritesRepository {
    suspend fun addFavorite(userToken: String, favoriteItem: FavoriteItem)

    suspend fun removeFavorite(userToken: String, productId: String)

    suspend fun getFavorites(userToken: String): List<FavoriteItemFirebase>

    suspend fun isFavorite(userToken: String, productId: String): Boolean
}