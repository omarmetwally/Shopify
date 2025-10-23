package com.omarinc.shopify.mocks

import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.favorites.model.FavoriteItemFirebase
import java.util.concurrent.ConcurrentHashMap


class FakeFirebaseFirestore {

    private val userFavorites = ConcurrentHashMap<String, MutableMap<String, FavoriteItem>>()

    fun addFavorite(userToken: String, productId: String, favoriteItem: FavoriteItem) {
        userFavorites.computeIfAbsent(userToken) { mutableMapOf() }[productId] = favoriteItem
    }

    fun removeFavorite(userToken: String, productId: String) {
        userFavorites[userToken]?.remove(productId)
    }

    fun getFavorites(userToken: String): List<FavoriteItemFirebase> {
        return userFavorites[userToken]?.values?.map {
            FavoriteItemFirebase(it.productId, it.productName, it.productPrice, it.productImage.toString())
        } ?: emptyList()
    }

    fun isFavorite(userToken: String, productId: String): Boolean {
        return userFavorites[userToken]?.containsKey(productId) == true
    }
}
