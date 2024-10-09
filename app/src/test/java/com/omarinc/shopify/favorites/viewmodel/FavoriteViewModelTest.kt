package com.omarinc.shopify.favorites.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omarinc.shopify.favorites.model.FavoriteItem
import com.omarinc.shopify.mocks.FakeFirebaseRepository
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavoriteViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeFirebaseRepository: FakeFirebaseRepository
    private lateinit var viewModel: FavoriteViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        fakeFirebaseRepository = FakeFirebaseRepository()
        viewModel = FavoriteViewModel(fakeFirebaseRepository)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addToFavorites_WithValidItem_ShouldAddFavoriteSuccessfully() = runTest {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url")

        viewModel.addToFavorites(userToken, favoriteItem)

        val isFavorite = viewModel.isFavorite.value
        assertTrue(isFavorite)
    }

    @Test
    fun removeFromFavorites_WithValidItem_ShouldRemoveFavoriteSuccessfully() = runTest {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url")

        viewModel.addToFavorites(userToken, favoriteItem)
        viewModel.removeFromFavorites(userToken, favoriteItem.productId)

        val isFavorite = viewModel.isFavorite.value
        assertFalse(isFavorite)
    }

    @Test
    fun checkIfFavorite_WithValidFavorite_ShouldReturnTrue() = runTest {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url")

        viewModel.addToFavorites(userToken, favoriteItem)
        viewModel.checkIfFavorite(userToken, favoriteItem.productId)

        val isFavorite = viewModel.isFavorite.value
        assertTrue(isFavorite)
    }

    @Test
    fun checkIfFavorite_WithNonExistentFavorite_ShouldReturnFalse() = runTest {
        val userToken = "fake_token"
        val productId = "non_existing_id"

        viewModel.checkIfFavorite(userToken, productId)

        val isFavorite = viewModel.isFavorite.value
        assertFalse(isFavorite)
    }

    @Test
    fun getFavorites_WithValidUserToken_ShouldReturnFavoritesList() = runTest {
        val userToken = "fake_token"
        val favoriteItem = FavoriteItem("fake_id", "product_name", 10.0, "image_url")

        viewModel.addToFavorites(userToken, favoriteItem)
        viewModel.getFavorites(userToken)

        val favorites = viewModel.favorites.value
        assertTrue(favorites.isNotEmpty())
        assertTrue(favorites.any { it.productId == favoriteItem.productId })
    }
}
