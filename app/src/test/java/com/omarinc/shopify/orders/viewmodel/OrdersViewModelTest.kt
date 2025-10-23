package com.omarinc.shopify.orders.viewmodel

import com.omarinc.shopify.mocks.FakeShopifyRepository
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.network.ApiState
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrdersViewModelTest {

    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp(){
        viewModel = OrdersViewModel(FakeShopifyRepository())
    }
    @Test
    fun getCustomerOrders_ShouldReturnListOfOrders() = runTest{
        viewModel.getCustomerOrders("fake_token")
        var res: ApiState<List<Order>>? = null
        viewModel.apiState.first {
            res = it
            true
        }
        assertNotNull(res)
        assertEquals(
            res,
            ApiState.Success<List<Order>>(
                emptyList<Order>()
            )
        )
    }
}