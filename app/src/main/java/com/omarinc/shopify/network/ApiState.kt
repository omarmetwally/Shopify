package com.omarinc.shopify.network

import com.omarinc.shopify.model.RegisterUserResponse


sealed class ApiState {
    class Success(val response: RegisterUserResponse) : ApiState()
    class Failure(val msg: Throwable) : ApiState()
    object Loading : ApiState()
}
