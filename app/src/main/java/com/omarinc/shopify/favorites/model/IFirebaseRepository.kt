package com.omarinc.shopify.favorites.model

interface IFirebaseRepository {
    suspend fun addFavorite(userToken: String, favoriteItem: FavoriteItem)

    suspend fun removeFavorite(userToken: String, productId: String)

    suspend fun getFavorites(userToken: String): List<FavoriteItemFirebase>

    suspend fun isFavorite(userToken: String, productId: String): Boolean

    suspend fun addCustomerCart(email: String, cartId: String)

    suspend fun isCustomerHasCart(email: String): Boolean

}