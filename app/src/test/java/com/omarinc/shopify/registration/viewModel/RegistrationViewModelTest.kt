package com.omarinc.shopify.registration.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.model.CustomerCreateData
import com.omarinc.shopify.CreateCustomerMutation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class RegistrationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: RegistrationViewModel
    private val repository: ShopifyRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegistrationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerUser_WithValidDetails_ShouldUpdateApiStateSuccessfully() = runTest {
        // Given
        val email = "test@testing.com"
        val password = "12345"
        val firstName = "test case"
        val phoneNumber = "1234567890"
        val expectedCustomer = CreateCustomerMutation.Customer("id", email, firstName, "omar")
        val expectedUserErrors = emptyList<CreateCustomerMutation.CustomerUserError>()
        val expectedCustomerCreateData = CustomerCreateData(expectedCustomer, expectedUserErrors)
        val expectedResponse = ApiState.Success(RegisterUserResponse(expectedCustomerCreateData))

        coEvery { repository.registerUser(email, password, firstName, phoneNumber) } returns flow {
            emit(expectedResponse)
        }

        // When
        viewModel.registerUser(email, password, firstName, phoneNumber)

        // Then
        val result = viewModel.apiState.first()
        assertEquals(expectedResponse, result)
    }
}
