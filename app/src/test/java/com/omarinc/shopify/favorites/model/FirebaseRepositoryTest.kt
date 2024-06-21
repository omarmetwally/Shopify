package com.omarinc.shopify.favorites.model

import com.omarinc.shopify.mocks.FakeFirebaseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FirebaseRepositoryTest {

    private lateinit var fakeFirebaseRepository: FakeFirebaseRepository

    @Before
    fun setUp() {
        fakeFirebaseRepository = FakeFirebaseRepository()
    }

    @Test
    fun addFavorite_WithValidData_ShouldAddFavoriteSuccessfully() = runBlocking {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url","")

        fakeFirebaseRepository.addFavorite(userToken, favoriteItem)
        val favorites = fakeFirebaseRepository.getFavorites(userToken)

        assertTrue(favorites.any { it.productId == favoriteItem.productId })
    }

    @Test
    fun removeFavorite_WithValidData_ShouldRemoveFavoriteSuccessfully() = runBlocking {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url","")

        fakeFirebaseRepository.addFavorite(userToken, favoriteItem)
        fakeFirebaseRepository.removeFavorite(userToken, favoriteItem.productId)
        val favorites = fakeFirebaseRepository.getFavorites(userToken)

        assertFalse(favorites.any { it.productId == favoriteItem.productId })
    }

    @Test
    fun isFavorite_WithAddedFavorite_ShouldReturnTrue() = runBlocking {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url","")

        fakeFirebaseRepository.addFavorite(userToken, favoriteItem)
        val isFavorite = fakeFirebaseRepository.isFavorite(userToken, favoriteItem.productId)

        assertTrue(isFavorite)
    }

    @Test
    fun isFavorite_WithNonAddedFavorite_ShouldReturnFalse() = runBlocking {
        val userToken = "fake_token"
        val productId = "non_existing_id"

        val isFavorite = fakeFirebaseRepository.isFavorite(userToken, productId)

        assertFalse(isFavorite)
    }

    @Test
    fun addCustomerCart_WithValidData_ShouldAddCartSuccessfully() = runBlocking {

    }

    @Test
    fun getCartByCustomer_WithValidData_ShouldReturnCorrectCartId() = runBlocking {

    }
}
