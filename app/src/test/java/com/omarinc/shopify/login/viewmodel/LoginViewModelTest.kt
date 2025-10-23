package com.omarinc.shopify.login.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.utilities.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private val repository: ShopifyRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginUser_WithValidCredentials_ShouldUpdateApiStateSuccessfully() = runTest {
        // Given
        val email = "test@test.com"
        val password = "12345"
        val expectedResponse = ApiState.Success("Login Successful")
        coEvery { repository.loginUser(email, password) } returns flow {
            emit(expectedResponse)
        }

        // When
        viewModel.loginUser(email, password)

        // Then
        val result = viewModel.apiState.first()
        assertEquals(expectedResponse, result)
    }

    @Test
    fun onSkipButtonPressed_ShouldUpdateSkipButtonStateSuccessfully() = runTest {
        // Given
        coEvery { repository.writeBooleanToSharedPreferences(Constants.USER_SKIPPED, true) } returns Unit

        // When
        viewModel.onSkipButtonPressed()

        // Then
        assertTrue(viewModel.skipButtonState.first())
        coVerify { repository.writeBooleanToSharedPreferences(Constants.USER_SKIPPED, true) }
    }
}
