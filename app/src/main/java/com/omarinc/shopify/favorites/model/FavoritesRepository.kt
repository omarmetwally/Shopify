package com.omarinc.shopify.favorites.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository private constructor() : IFavoritesRepository {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        @Volatile
        private var instance: FavoritesRepository? = null

        fun getInstance(): FavoritesRepository {
            return instance ?: synchronized(this) {
                instance ?: FavoritesRepository().also { instance = it }
            }
        }
    }

    override suspend fun addFavorite(userToken: String, favoriteItem: FavoriteItem) {
        val cleanedProductId = getProductIdWithoutPrefix(favoriteItem.productId)
        db.collection("users").document(userToken).collection("favorites")
            .document(cleanedProductId).set(favoriteItem).await()
    }

    override suspend fun removeFavorite(userToken: String, productId: String) {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        db.collection("users").document(userToken).collection("favorites").document(cleanedProductId)
            .delete().await()
    }

    override suspend fun getFavorites(userToken: String): List<FavoriteItemFirebase> {
        return db.collection("users").document(userToken).collection("favorites").get().await()
            .toObjects(FavoriteItemFirebase::class.java)
    }

    override suspend fun isFavorite(userToken: String, productId: String): Boolean {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        val document =
            db.collection("users").document(userToken).collection("favorites").document(cleanedProductId)
                .get().await()
        return document.exists()
    }
    private fun getProductIdWithoutPrefix(productId: String): String {
        return productId.substringAfterLast("/")
    }
}
