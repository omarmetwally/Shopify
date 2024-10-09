package com.omarinc.shopify.network

import com.omarinc.shopify.model.RegisterUserResponse



sealed class ApiState<out T> {
    class Success<out T>(val response: T) : ApiState<T>()
    class Failure(val msg: Throwable) : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
}