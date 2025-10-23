package com.omarinc.shopify.search.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.productdetails.model.Products
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class SearchViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: SearchViewModel
    private val repository: ShopifyRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchProducts_WithValidQuery_ShouldUpdateSearchResultsSuccessfully() = runTest {
        // Given
        val query = "test"
        val expectedProducts = listOf(Products("1", "Product 1", "s", "", ""), Products("2", "Product 2", "", "", ""))
        coEvery { repository.searchProducts(query) } returns expectedProducts

        // When
        viewModel.searchProducts(query)

        // Then
        val result = viewModel.searchResults.first()
        assertEquals(expectedProducts, result)
    }
}
