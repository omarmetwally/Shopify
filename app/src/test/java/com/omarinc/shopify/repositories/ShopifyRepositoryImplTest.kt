package com.omarinc.shopify.repositories

import com.omarinc.shopify.mocks.FakeAdminRemoteDataSource
import com.omarinc.shopify.mocks.FakeCurrencyRemoteDataSource
import com.omarinc.shopify.mocks.FakeSharedPreferences
import com.omarinc.shopify.mocks.FakeShopifyRemoteDataSource
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ShopifyRepositoryImplTest {

    private lateinit var fakeShopifyRemoteDataSource: FakeShopifyRemoteDataSource
    private lateinit var repository: ShopifyRepository

    @Before
    fun setUp() {
        fakeShopifyRemoteDataSource = FakeShopifyRemoteDataSource()
        repository = ShopifyRepositoryImpl
            .getInstance(
                FakeShopifyRemoteDataSource(),
                FakeSharedPreferences(),
                FakeCurrencyRemoteDataSource(),
                FakeAdminRemoteDataSource()
            )
    }

    @Test
    fun getBrandsSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getBrands()
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    Assert.assertNotNull(state.response.get(0))
                    Assert.assertEquals("1", state.response.get(0).id)
                }
                else -> Assert.fail("Expected success state")
            }
        }
    }
    @Test
    fun getProductsByBrandIdFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getProductsByBrandId("1")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    Assert.assertNotNull(state.msg)
                    Assert.assertEquals("Fake error", state.msg.message)
                }
                else -> Assert.fail("Expected failure state")
            }
        }
    }
    @Test
    fun getProductsByBrandIdSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getProductsByBrandId("1")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    Assert.assertNotNull(state.response.get(0))
                    Assert.assertEquals("1", state.response.get(0).id)
                }
                else -> Assert.fail("Expected success state")
            }
        }
    }
    @Test
    fun getCollectionByHandleFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getCollectionByHandle("fake_handle")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    Assert.assertNotNull(state.msg)
                    Assert.assertEquals("Fake error", state.msg.message)
                }
                else -> Assert.fail("Expected failure state")
            }
        }
    }
    @Test
    fun getPCollectionByHandleSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getCollectionByHandle("men")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    Assert.assertNotNull(state.response)
                    Assert.assertEquals("1", state.response.id)
                }
                else -> Assert.fail("Expected success state")
            }
        }
    }

    @Test
    fun getCustomerOrdersFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getCustomerOrders("fake_token")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    Assert.assertNotNull(state.msg)
                    Assert.assertEquals("Fake error", state.msg.message)
                }
                else -> Assert.fail("Expected failure state")
            }
        }
    }

    @Test
    fun getCustomerOrdersSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getCustomerOrders("token")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    Assert.assertNotNull(state.response)
                    Assert.assertEquals(emptyList<Order>(), state.response)
                }
                else -> Assert.fail("Expected success state")
            }
        }
    }

    @Test
    fun getProductsByTypeFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getProductByType("shoes")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    Assert.assertNotNull(state.msg)
                    Assert.assertEquals("Fake error", state.msg.message)
                }
                else -> Assert.fail("Expected failure state")
            }
        }
    }

    @Test
    fun getProductsByTypeSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getProductByType("shoes")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    Assert.assertNotNull(state.response.get(0))
                    Assert.assertEquals("1", state.response.get(0).id)
                }
                else -> Assert.fail("Expected success state")
            }
        }
    }
}