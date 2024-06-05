package com.omarinc.shopify.favorites.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.omarinc.shopify.utilities.Constants.CUSTOMER_CART_ROOT
import kotlinx.coroutines.tasks.await

class FavoritesRepository private constructor() : IFavoritesRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val database: DatabaseReference = Firebase.database.reference

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
        firestore.collection("users").document(userToken).collection("favorites")
            .document(cleanedProductId).set(favoriteItem).await()
    }

    override suspend fun removeFavorite(userToken: String, productId: String) {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        firestore.collection("users").document(userToken).collection("favorites")
            .document(cleanedProductId)
            .delete().await()
    }

    override suspend fun getFavorites(userToken: String): List<FavoriteItemFirebase> {
        return firestore.collection("users").document(userToken).collection("favorites").get()
            .await()
            .toObjects(FavoriteItemFirebase::class.java)
    }

    override suspend fun isFavorite(userToken: String, productId: String): Boolean {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        val document =
            firestore.collection("users").document(userToken).collection("favorites")
                .document(cleanedProductId)
                .get().await()
        return document.exists()
    }


    private fun getProductIdWithoutPrefix(productId: String): String {
        return productId.substringAfterLast("/")
    }

    override suspend fun addCustomerCart(email: String, cartId: Int) {
        try {


            database.child(CUSTOMER_CART_ROOT).child(email).setValue(cartId).await()
        } catch (e: Exception) {

            throw e
        }
    }

    override suspend fun isCustomerHasCart(email: String): Boolean {

        return try {
            val snapshot = database.child(CUSTOMER_CART_ROOT).child(email).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            throw e
        }
    }


}
