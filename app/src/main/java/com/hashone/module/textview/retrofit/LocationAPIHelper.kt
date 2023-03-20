package com.hashone.module.textview.retrofit

import com.hashone.module.textview.retrofit.repository.Api
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationAPIHelper {
    companion object {
        private val retrofits by lazy {
            val client = OkHttpClient.Builder()
                .build()
            Retrofit.Builder()
                .baseUrl("http://ip-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val apiIPInstance: Api by lazy { retrofits.create(Api::class.java) }
    }
}