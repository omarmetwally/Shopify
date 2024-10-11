package com.omarinc.shopify.mocks


import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.favorites.model.FavoriteItemFirebase
import com.omarinc.shopify.favorites.model.IFirebaseRepository
import com.omarinc.shopify.utilities.Helper.encodeEmail


class FakeFirebaseRepository : IFirebaseRepository {

    private val fakeFirestore = FakeFirebaseFirestore()
    private val fakeDatabase = FakeDatabaseReference()

    override suspend fun addFavorite(userToken: String, favoriteItem: FavoriteItem) {
        val cleanedProductId = getProductIdWithoutPrefix(favoriteItem.productId)
        fakeFirestore.addFavorite(userToken, cleanedProductId, favoriteItem)
    }

    override suspend fun removeFavorite(userToken: String, productId: String) {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        fakeFirestore.removeFavorite(userToken, cleanedProductId)
    }

    override suspend fun getFavorites(userToken: String): List<FavoriteItemFirebase> {
        return fakeFirestore.getFavorites(userToken)
    }

    override suspend fun isFavorite(userToken: String, productId: String): Boolean {
        val cleanedProductId = getProductIdWithoutPrefix(productId)
        return fakeFirestore.isFavorite(userToken, cleanedProductId)
    }

    override suspend fun addCustomerCart(email: String, cartId: String) {
        fakeDatabase.addCustomerCart(email, cartId)
    }

    override suspend fun isCustomerHasCart(email: String): Boolean {
        return fakeDatabase.isCustomerHasCart(email)
    }

    override suspend fun getCartByCustomer(email: String): String {
        return fakeDatabase.getCartByCustomer(email)
    }


    private fun getProductIdWithoutPrefix(productId: String): String {
        return productId.substringAfterLast("/")
    }
}
