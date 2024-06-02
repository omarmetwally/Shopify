package com.omarinc.shopify.hilt

import android.content.Context
import android.content.SharedPreferences
import com.apollographql.apollo3.ApolloClient
import com.omarinc.shopify.network.currency.CurrencyApiService
import com.omarinc.shopify.utilities.Constants
import com.omarinc.shopify.utilities.Constants.CURRENCY_API_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class FirstModuleProvider {

    @Provides
    @Singleton
    fun provideCurrencyRetrofitClient():Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(CURRENCY_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        return retrofit
    }

    @Provides
    @Singleton
    fun provideCurrencyService(): CurrencyApiService {
           return provideCurrencyRetrofitClient().create(CurrencyApiService::class.java)
        }

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(Constants.BASE_URL_GRAPHQL)
            .addHttpHeader(Constants.ACCESS_TOKEN_KEY, Constants.ACCESS_TOKEN_VALUE)
            .build()
    }

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
}