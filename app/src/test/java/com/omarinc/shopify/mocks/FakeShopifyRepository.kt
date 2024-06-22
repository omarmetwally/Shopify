package com.omarinc.shopify.mocks

import com.omarinc.shopify.CustomerDetailsQuery
import com.omarinc.shopify.model.RegisterUserResponse
import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.models.Brands
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.models.CheckoutResponse
import com.omarinc.shopify.models.Collection
import com.omarinc.shopify.models.CurrencyResponse
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.models.DiscountCodesResponse
import com.omarinc.shopify.models.DraftOrderRequest
import com.omarinc.shopify.models.DraftOrderResponse
import com.omarinc.shopify.models.Order
import com.omarinc.shopify.models.PriceRulesResponse
import com.omarinc.shopify.models.Product
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.productdetails.model.ProductDetails
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.type.CheckoutLineItemInput
import kotlinx.coroutines.flow.Flow

class FakeShopifyRepository : ShopifyRepository {

  private val shopifyRemoteDataSource =  FakeShopifyRemoteDataSource()
    private val sharedPreferences = FakeSharedPreferences()
    private val currencyRemoteDataSource = FakeCurrencyRemoteDataSource()
    private val adminRemoteDataSource = FakeAdminRemoteDataSource()
    override suspend fun registerUser(
        email: String,
        password: String,
        firstName: String,
        phoneNumber: String
    ): Flow<ApiState<RegisterUserResponse>> {
       return shopifyRemoteDataSource.registerUser(email,password,firstName,phoneNumber)
    }

    override fun getBrands(): Flow<ApiState<List<Brands>>> {
        return shopifyRemoteDataSource.getBrands()
    }

    override fun getProductsByBrandId(id: String): Flow<ApiState<List<Product>>> {
        return shopifyRemoteDataSource.getProductsByBrandId(id)
    }

    override suspend fun loginUser(email: String, password: String): Flow<ApiState<String>> {
        return shopifyRemoteDataSource.loginUser(email,password)
    }

    override suspend fun writeBooleanToSharedPreferences(key: String, value: Boolean) {
        return sharedPreferences.writeBooleanToSharedPreferences(key,value)
    }

    override suspend fun readBooleanFromSharedPreferences(key: String): Boolean {
        return sharedPreferences.readBooleanFromSharedPreferences(key)
    }

    override suspend fun readUserToken(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrencyRate(requiredCurrency: String): Flow<ApiState<CurrencyResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun writeCurrencyRate(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun writeCurrencyUnit(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override suspend fun readCurrencyRate(key: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun readCurrencyUnit(key: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getProductById(productId: String): Flow<ApiState<ProductDetails>> {
        TODO("Not yet implemented")
    }

    override suspend fun searchProducts(query: String): List<Products> {
        TODO("Not yet implemented")
    }

    override fun getCustomerOrders(token: String): Flow<ApiState<List<Order>>> {
        return shopifyRemoteDataSource.getCustomerOrders(token)
    }
    override fun getProductByType(type: String): Flow<ApiState<List<Product>>> {
        return shopifyRemoteDataSource.getProductByType(type)
    }
    override fun getCollectionByHandle(handle: String): Flow<ApiState<Collection>> {
        return shopifyRemoteDataSource.getCollectionByHandle(handle)
    }
    override suspend fun createCart(token: String): Flow<ApiState<String?>> {
        TODO("Not yet implemented")
    }

    override suspend fun readEmailFromSharedPreferences(key: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun addToCartById(
        cartId: String,
        quantity: Int,
        variantID: String
    ): Flow<ApiState<String?>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeProductFromCart(
        cartId: String,
        lineId: String
    ): Flow<ApiState<String?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCartProducts(cartId: String): Flow<ApiState<List<CartProduct>>> {
        TODO("Not yet implemented")
    }

    override suspend fun createAddress(
        customerAddress: CustomerAddress,
        token: String
    ): Flow<ApiState<String?>> {
        TODO("Not yet implemented")
    }

    override suspend fun writeCartIdToSharedPreferences(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun readCartIdFromSharedPreferences(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getCoupons(): Flow<ApiState<PriceRulesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCouponDetails(couponId: String): Flow<ApiState<DiscountCodesResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun getCustomerAddresses(token: String): Flow<ApiState<List<CustomerAddress>>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCustomerAddress(
        addressId: String,
        token: String
    ): Flow<ApiState<String?>> {
        TODO("Not yet implemented")
    }

    override suspend fun createDraftOrder(draftOrder: DraftOrderRequest): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun completeDraftOrder(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendInvoice(orderId: Long): Flow<ApiState<DraftOrderResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun writeIsFirstTimeUser(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun readIsFirstTimeUser(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun clearAllData() {
        TODO("Not yet implemented")
    }

    override fun getCustomerDetails(token: String): Flow<ApiState<CustomerDetailsQuery.Customer>> {
        TODO("Not yet implemented")
    }

    override suspend fun createCheckout(
        lineItems: List<CheckoutLineItemInput>,
        email: String?
    ): Flow<ApiState<CheckoutResponse?>> {
        TODO("Not yet implemented")
    }
}