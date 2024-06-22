package com.omarinc.shopify.categories.viewmodel

import com.omarinc.shopify.mocks.FakeShopifyRepository
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CategoriesViewModelTest {

    private lateinit var viewModel: CategoriesViewModel
    @Before
    fun setUp(){
        viewModel = CategoriesViewModel(FakeShopifyRepository())
    }
    @Test
    fun filterProducts_shouldReturnListOfProducts() = runTest {
        viewModel.filterProducts(mutableListOf())
        var res: ApiState<List<Product>>? = null
        viewModel.apiState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(res,
            ApiState.Success(
                mutableListOf<Product>())
        )
    }
    @Test
    fun getProductsByType_withType_shouldReturnListOfProductsOfThisType() = runTest {
        viewModel.getCollectionByHandle("men")
        viewModel.getProductsByType("men")
        var res: ApiState<List<Product>>? = null
        viewModel.apiState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(res,
            ApiState.Success(
               mutableListOf<Product>())
            )
    }
    @Test
    fun getCollectionByHandle_WithValidHandle_shouldReturnTheRightCollection() = runTest {
        viewModel.getCollectionByHandle("men")
        var res: ApiState<Collection>? = null
        viewModel.collectionApiState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(res,
            ApiState.Success<Collection>(
                Collection("1", "Collection1", "desc1", emptyList())))
    }

}