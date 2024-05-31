package com.omarinc.shopify.model

import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.network.ShopifyRemoteDataSource
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSource
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopifyRepositoryImpl(
    private val shopifyRemoteDataSource: ShopifyRemoteDataSource,
    private val sharedPreferences: ISharedPreferences,
    private val currencyRemoteDataSource: CurrencyRemoteDataSource
) : ShopifyRepository {

    companion object {
        @Volatile
        private var instance: ShopifyRepositoryImpl? = null

        fun getInstance(
            shopifyRemoteDataSource: ShopifyRemoteDataSource,
            sharedPreferences: ISharedPreferences,
            currencyRemoteDataSource: CurrencyRemoteDataSource
        ): ShopifyRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: ShopifyRepositoryImpl(
                    shopifyRemoteDataSource,
                    sharedPreferences,
                    currencyRemoteDataSource
                ).also { instance = it }
            }
        }
    }

    override suspend fun registerUser(
        email: String,
        password: String,
        fullName: String
    ): Flow<ApiState<RegisterUserResponse>> {
        return shopifyRemoteDataSource.registerUser(email, password, fullName)
    }

    override suspend fun loginUser(email: String, password: String): Flow<ApiState<String>> {
        return shopifyRemoteDataSource.loginUser(email, password).map { state ->
            if (state is ApiState.Success) {
                sharedPreferences.writeStringToSharedPreferences(
                    Constants.USER_TOKEN,
                    state.response
                )
            }
            state
        }
    }

    override suspend fun writeBooleanToSharedPreferences(key: String, value: Boolean) {
        sharedPreferences.writeBooleanToSharedPreferences(key, value)
    }

    override suspend fun readBooleanFromSharedPreferences(key: String): Boolean {
        return sharedPreferences.readBooleanFromSharedPreferences(key)
    }

    override suspend fun readUserToken(): String {
        return sharedPreferences.readStringFromSharedPreferences(Constants.USER_TOKEN)
    }

    override suspend fun getCurrencyRate(requiredCurrency: Currencies): Flow<ApiState<CurrencyResponse>> {
        return currencyRemoteDataSource.getCurrencyRate(requiredCurrency)
    }

    override suspend fun writeCurrencyRate(key: String, value: Long) {
        sharedPreferences.writeCurrencyRateToSharedPreferences(key, value)
    }

    override suspend fun writeCurrencyUnit(key: String, value: String) {
        sharedPreferences.writeCurrencyUnitToSharedPreferences(key, value)
    }

    override suspend fun readCurrencyRate(key: String): Long {
        return readCurrencyRate(key)
    }

    override suspend fun readCurrencyUnit(key: String): String {
        return readCurrencyUnit(key)
    }

    override suspend fun getProductById(productId: String): Flow<ApiState<ProductDetails>> {
        return shopifyRemoteDataSource.getProductById(productId)
    }


}
