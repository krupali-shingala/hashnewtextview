package com.hashone.module.textview.retrofit

sealed class Resource<T>(
    val data: T? = null, val message: String? = null, val errorCode: Int? = -1
) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, errorCode: Int? = 500, data: T? = null) :
        Resource<T>(data, message, errorCode)

    class Loading<T> : Resource<T>()
}