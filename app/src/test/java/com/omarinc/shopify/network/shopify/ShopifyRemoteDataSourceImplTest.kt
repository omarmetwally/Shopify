package com.omarinc.shopify.network.shopify


import com.omarinc.shopify.mocks.FakeShopifyRemoteDataSource
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShopifyRemoteDataSourceImplTest {

    private lateinit var fakeShopifyRemoteDataSource: FakeShopifyRemoteDataSource

    @Before
    fun setUp() {
        fakeShopifyRemoteDataSource = FakeShopifyRemoteDataSource()
    }

    @Test
    fun registerUserSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.registerUser("omar@example.com", "password", "omar", "phoneNumber")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    assertNotNull(state.response)
                    assertEquals("omar@example.com",
                        state.response.customerCreate.customer?.email ?: ""
                    )
                }
                else -> fail("Expected success state")
            }
        }
    }

    @Test
    fun registerUserFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.registerUser("email@example.com", "password", "fullName", "phoneNumber")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
            }
        }
    }

    @Test
    fun loginUserSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.loginUser("email@example.com", "password")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    assertNotNull(state.response)
                    assertEquals("fake_token", state.response)
                }
                else -> fail("Expected success state")
            }
        }
    }

    @Test
    fun loginUserFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.loginUser("email@example.com", "password")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
            }
        }
    }

    @Test
    fun getCustomerDetailsSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getCustomerDetails("fake_token")
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    assertNotNull(state.response.firstName)
                    assertEquals("omar", state.response.firstName)
                }
                else -> fail("Expected success state")
            }
        }
    }
    @Test
    fun getCustomerDetailsFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getCustomerDetails("fake_token")
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
            }
        }
    }
    @Test
    fun getBrandsFailure() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = true

        val flow = fakeShopifyRemoteDataSource.getBrands()
        flow.collect { state ->
            when (state) {
                is ApiState.Failure -> {
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
            }
        }
    }
    @Test
    fun getBrandsSuccess() = runBlocking {
        fakeShopifyRemoteDataSource.shouldReturnError = false

        val flow = fakeShopifyRemoteDataSource.getBrands()
        flow.collect { state ->
            when (state) {
                is ApiState.Success -> {
                    assertNotNull(state.response.get(0))
                    assertEquals("1", state.response.get(0).id)
                }
                else -> fail("Expected success state")
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
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
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
                    assertNotNull(state.response.get(0))
                    assertEquals("1", state.response.get(0).id)
                }
                else -> fail("Expected success state")
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
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
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
                    assertNotNull(state.response)
                    assertEquals("1", state.response.id)
                }
                else -> fail("Expected success state")
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
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
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
                    assertNotNull(state.response)
                    assertEquals(emptyList<Order>(), state.response)
                }
                else -> fail("Expected success state")
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
                    assertNotNull(state.msg)
                    assertEquals("Fake error", state.msg.message)
                }
                else -> fail("Expected failure state")
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
                    assertNotNull(state.response.get(0))
                    assertEquals("1", state.response.get(0).id)
                }
                else -> fail("Expected success state")
            }
        }
    }

}
