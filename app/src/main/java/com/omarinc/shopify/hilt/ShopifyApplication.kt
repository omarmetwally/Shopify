package com.omarinc.shopify.hilt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShopifyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}