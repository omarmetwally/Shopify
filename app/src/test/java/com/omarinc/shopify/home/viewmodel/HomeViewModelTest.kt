package com.omarinc.shopify.home.viewmodel

import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.mocks.FakeFirebaseRepository
import com.omarinc.shopify.mocks.FakeShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp(){
        viewModel = HomeViewModel(FakeShopifyRepository(), FirebaseRepository.getInstance())
    }
    @Test
    fun getBrands_ShouldReturnListOfBrands() = runTest{
        viewModel.getBrands()
        var res: ApiState<List<Brands>>? = null
        viewModel.brandsState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(
            res,
            ApiState.Success<List<Brands>>(
                listOf(Brands("1", "Brand1", "url1"), Brands("2", "Brand2", "url2"))
            )
        )
    }

    @Test
    fun getProductsByBrandId_WithValidId_shouldReturnListOfProducts() = runTest{
        viewModel.getProductsByBrandId("fake_id")
        var res: ApiState<List<Product>>? = null
        viewModel.productsState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(
            res,
            ApiState.Success<List<Product>>(
                listOf(Product("1", "Product1", "handle1", "desc1", "image1", "type1", "10.0", "USD"))
            )
        )
    }
}