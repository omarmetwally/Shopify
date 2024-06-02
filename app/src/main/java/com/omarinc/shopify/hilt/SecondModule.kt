package com.omarinc.shopify.hilt

import com.omarinc.shopify.model.ShopifyRepository
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ShopifyRemoteDataSource
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSource
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.ISharedPreferences
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class SecondModule {
        @Binds
        @Singleton
        abstract fun provideShopifyRemoteDataSource(impl:ShopifyRemoteDataSourceImpl)
                :ShopifyRemoteDataSource

        @Binds
        @Singleton
        abstract fun provideCurrencyLocalDataSource(impl: CurrencyRemoteDataSourceImpl)
                :CurrencyRemoteDataSource

        @Binds
        @Singleton
        abstract fun provideSharedPreferences(impl: SharedPreferencesImpl)
                :ISharedPreferences

        @Binds
        @Singleton
        abstract fun provideRepo(repo:ShopifyRepositoryImpl):ShopifyRepository
    }
