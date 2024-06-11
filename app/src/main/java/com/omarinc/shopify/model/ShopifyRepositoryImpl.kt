package com.omarinc.shopify.model

import android.util.Log
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSource
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSource
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSource
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopifyRepositoryImpl(
    private val shopifyRemoteDataSource: ShopifyRemoteDataSource,
    private val sharedPreferences: ISharedPreferences,
    private val currencyRemoteDataSource: CurrencyRemoteDataSource,
    private val adminRemoteDataSource: AdminRemoteDataSource
) : ShopifyRepository {

    companion object {
        @Volatile
        private var instance: ShopifyRepositoryImpl? = null

        fun getInstance(
            shopifyRemoteDataSource: ShopifyRemoteDataSource,
            sharedPreferences: ISharedPreferences,
            currencyRemoteDataSource: CurrencyRemoteDataSource,
            adminRemoteDataSource: AdminRemoteDataSource
        ): ShopifyRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: ShopifyRepositoryImpl(
                    shopifyRemoteDataSource,
                    sharedPreferences,
                    currencyRemoteDataSource,
                    adminRemoteDataSource
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
        return shopifyRemoteDataSource.getBrands()
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
                sharedPreferences.writeStringToSharedPreferences(
                    Constants.USER_EMAIL,
                    email
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

    override suspend fun getCurrencyRate(requiredCurrency: String): Flow<ApiState<CurrencyResponse>> {
        return currencyRemoteDataSource.getCurrencyRate(requiredCurrency)
    }

    override suspend fun writeCurrencyRate(key: String, value: Long) {
        sharedPreferences.writeCurrencyRateToSharedPreferences(key, value)
    }

    override suspend fun writeCurrencyUnit(key: String, value: String) {
        sharedPreferences.writeCurrencyUnitToSharedPreferences(key, value)
    }

    override suspend fun readCurrencyRate(key: String): Long {
        return sharedPreferences.readCurrencyRateFromSharedPreferences(key)
    }

    override suspend fun readCurrencyUnit(key: String): String {
        return sharedPreferences.readCurrencyUnitFromSharedPreferences(key)
    }

    override suspend fun getProductById(productId: String): Flow<ApiState<ProductDetails>> {
        return shopifyRemoteDataSource.getProductById(productId)
    }


    override suspend fun searchProducts(query: String): List<Products> {
        return shopifyRemoteDataSource.searchProducts(query)
    }

    override fun getCutomerOrders(token: String): Flow<ApiState<List<Order>>> {
        return shopifyRemoteDataSource.getCutomerOrders(token)
    }

    override fun getProductByType(type: String): Flow<ApiState<List<Product>>> {
        return shopifyRemoteDataSource.getProductByType(type)
    }

    override fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>> {
        return shopifyRemoteDataSource.getCollectionByHandle(handle)
    }

    override suspend fun createCart(token: String): Flow<ApiState<String?>> {
        return shopifyRemoteDataSource.createCart(token)
    }

    override suspend fun readEmailFromSharedPreferences(key: String): String {
        return sharedPreferences.readStringFromSharedPreferences(key)
    }

    override suspend fun getCartProducts(cartId: String): Flow<ApiState<List<CartProduct>>> {
        return shopifyRemoteDataSource.getProductsCart(cartId)
    }

    override suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>> {
        return shopifyRemoteDataSource.createAddress(customerAddress, token)
    }

    override suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>> {
        return adminRemoteDataSource.getCoupons()
    }

    override suspend fun getCouponDetails(couponId:String): Flow<ApiState<DiscountCodesResponse>> {
        return adminRemoteDataSource.getCouponDetails(couponId)
    }

    override suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>> {
        Log.i("TAG", "createDraftOrder: repo")
        return adminRemoteDataSource.createDraftOrder(draftOrder)
    }

    override suspend fun completeDraftOrder(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        return adminRemoteDataSource.completeDraftOrder(orderId)
    }

    override suspend fun sendInvoice(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        return adminRemoteDataSource.sendInvoice(orderId)
    }


}
