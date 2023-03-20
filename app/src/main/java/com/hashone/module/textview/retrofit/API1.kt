package com.hashone.module.textview.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface API1 {

    @GET("{path}")
    suspend fun getData(@Path("path") path: String): Response<ResponseBody>
}