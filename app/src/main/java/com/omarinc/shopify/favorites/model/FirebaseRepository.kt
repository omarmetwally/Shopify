package com.omarinc.shopify.favorites.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.omarinc.shopify.utilities.Constants.CUSTOMER_CART_ROOT
import com.omarinc.shopify.utilities.Helper.encodeEmail
import kotlinx.coroutines.tasks.await

class FirebaseRepository private constructor() : IFirebaseRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val database: DatabaseReference = Firebase.database.reference

    companion object {
        @Volatile
        private var instance: FirebaseRepository? = null

        fun getInstance(): FirebaseRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseRepository().also { instance = it }
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

    override suspend fun addCustomerCart(email: String, cartId: String) {
        try {
            val encodedEmail = encodeEmail(email)
            database.child(CUSTOMER_CART_ROOT).child(encodedEmail).setValue(cartId).await()
        } catch (e: Exception) {

            throw e
        }
    }

    override suspend fun isCustomerHasCart(email: String): Boolean {

        return try {
            val encodedEmail = encodeEmail(email)
            val snapshot = database.child(CUSTOMER_CART_ROOT).child(encodedEmail).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getCartByCustomer(email: String): String {
        return try {
            val encodedEmail = encodeEmail(email)
            val snapshot = database.child(CUSTOMER_CART_ROOT).child(encodedEmail).get().await()
            if (snapshot.exists()) {
                snapshot.getValue(String::class.java) ?: throw IllegalStateException("Cart ID is null")
            } else {
                throw IllegalStateException("No cart found for the given customer")
            }
        } catch (e: Exception) {
            throw e
        }
    }



}
