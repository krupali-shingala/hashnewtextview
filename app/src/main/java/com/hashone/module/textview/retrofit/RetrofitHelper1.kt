package com.hashone.module.textview.retrofit

import com.hashone.commonutils.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitHelper1 {

    private val SERVER_URL = "http://ip-api.com/"

    private lateinit var gsonAPI: API1
    private var connectionCallBack: ConnectionCallBack? = null

    constructor() {
        val gsonRetrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(
                getClient()
            )
            .addConverterFactory(GsonConverterFactory.create(Utils.getGson()))
            .build()

        gsonAPI = gsonRetrofit.create(API1::class.java)
    }

    private fun getClient(): OkHttpClient {
        val okHttpClient = OkHttpClient()
        val TIMEOUT = 10 * 1000
        return okHttpClient.newBuilder()
            .connectTimeout(
                TIMEOUT.toLong(), TimeUnit.SECONDS
            ).readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(
                TIMEOUT.toLong(), TimeUnit.SECONDS
            ).build()
    }

    @Provides
    @Singleton
    fun api(): API1 {
        return gsonAPI
    }

    interface ConnectionCallBack {
        fun onSuccess(body: Response<ResponseBody>)

        fun onError(code: Int, error: String?)
    }
}