package com.omarinc.shopify.model

import com.omarinc.shopify.GetBrandsQuery
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ShopifyRemoteDataSource
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopifyRepositoryImpl(
    private val shopifyRemoteDataSource: ShopifyRemoteDataSource,
    private val sharedPreferences: ISharedPreferences
) : ShopifyRepository {

    companion object {
        @Volatile
        private var instance: ShopifyRepositoryImpl? = null

        fun getInstance(
            shopifyRemoteDataSource: ShopifyRemoteDataSource,
            sharedPreferences: ISharedPreferences
        ): ShopifyRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: ShopifyRepositoryImpl(
                    shopifyRemoteDataSource,
                    sharedPreferences
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

    override fun getBrands(): Flow<ApiState<List<Brands>>> {
        return  shopifyRemoteDataSource.getBrands()
    }

    override fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>> {
        return shopifyRemoteDataSource.getProductsByBrandId(id)
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
}
