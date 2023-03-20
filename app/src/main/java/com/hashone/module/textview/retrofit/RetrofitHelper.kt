package com.hashone.module.textview.retrofit

import android.accounts.NetworkErrorException
import android.text.TextUtils
import com.hashone.module.textview.retrofit.repository.Api
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.base.MyApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.UnknownHostException
import java.text.ParseException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitHelper {

    private val SERVER_URL = "${Constants.LIVE_DOMAIN}api/applications/"

    //    private val SERVER_URL = "${Constants.TEST_DOMAIN}api/applications/"
    private lateinit var gsonAPI: API
    private lateinit var gsonAPIs: Api
    private var connectionCallBack: ConnectionCallBack? = null

    var TOKEN =
        "Z3Ewd0lZMjl0TEdNVjdtRnBieTVsWkdsMGIzSXVaR1Z6YVdkdUxuUmxiWEJzWVhSbExtWnZjaTV6YjJOcFlXd3ViV1ZrYVdFPVo3SXJhUA=="

    private external fun show(): String

    constructor() {
        val gsonRetrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(
                getClient()
            )
            .addConverterFactory(GsonConverterFactory.create(Utils.getGson()))
            .build()

        gsonAPI = gsonRetrofit.create(API::class.java)
        gsonAPIs = gsonRetrofit.create(Api::class.java)
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

    private fun getClient1(): OkHttpClient {
        val okHttpClient = OkHttpClient()
        val TIMEOUT = 4 * 1000
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
    fun api(): API {
        return gsonAPI
    }

    @Provides
    @Singleton
    fun apis(): Api {
        return gsonAPIs
    }

    fun getFieldMap(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        if (TextUtils.isEmpty(TOKEN)) {
            TOKEN = show()
        }
        hashMap["token"] = TOKEN
        if (MyApplication.instance.context != null) {
            val uniqueToken =
                MyApplication.instance.storeUserData.getString(Constants.ANDROID_DEVICE_TOKEN)
            if (uniqueToken != null && uniqueToken.isNotEmpty())
                hashMap["unique_token"] =
                    MyApplication.instance.storeUserData.getString(Constants.ANDROID_DEVICE_TOKEN)!!
        }
        hashMap["t"] = "${System.currentTimeMillis()}"

        return hashMap
    }

    fun getPartMap(): HashMap<String, RequestBody> {
        val hashMap = HashMap<String, RequestBody>()
        if (TextUtils.isEmpty(TOKEN)) {
            TOKEN = show()
        }
        hashMap["token"] = toRequestBody(TOKEN)
        hashMap["t"] = toRequestBody("${System.currentTimeMillis()}")
        return hashMap
    }

    fun toRequestBody(value: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }

    fun prepareCondition(key: String, condition: String, value: Any): JSONObject {
        val jsonObject2 = JSONObject()
        jsonObject2.put("key", key)
        jsonObject2.put("condition", condition)
        jsonObject2.put("value", value)
        return jsonObject2
    }

    fun orderByCondition(key: String, value: Any): JSONObject {
        val jsonObject2 = JSONObject()
        jsonObject2.put("by", key)
        jsonObject2.put("type", value)
        return jsonObject2
    }

    fun callApi(call: Call<ResponseBody>, callBack: ConnectionCallBack) {
        connectionCallBack = callBack
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
                var message: String? = null
                if (error is NetworkErrorException) {
                    message = "Please check your internet connection"
                    if (connectionCallBack != null)
                        connectionCallBack!!.onError(1000, message)
                } else if (error is ParseException) {
                    message = "Parsing error! Please try again after some time!!"
                    if (connectionCallBack != null)
                        connectionCallBack!!.onError(-1, message)
                } else if (error is TimeoutException) {
                    message = "Connection TimeOut! Please check your internet connection."
                    if (connectionCallBack != null)
                        connectionCallBack!!.onError(1000, message)
                } else if (error is UnknownHostException) {
                    message = "Please check your internet connection and try later"
                    if (connectionCallBack != null)
                        connectionCallBack!!.onError(1000, message)
                } else if (error is Exception) {
                    message = error.message
                    if (connectionCallBack != null)
                        connectionCallBack!!.onError(-1, message)
                }
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (connectionCallBack != null)
                        connectionCallBack!!.onSuccess(response)
                } else {
                    try {
                        val res = response.body()?.string()
                        if (connectionCallBack != null)
                            connectionCallBack!!.onError(response.code(), res)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        if (connectionCallBack != null)
                            connectionCallBack!!.onError(response.code(), e.message)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        if (connectionCallBack != null)
                            connectionCallBack!!.onError(response.code(), e.message)
                    }
                }
            }
        })
    }

    interface ConnectionCallBack {
        fun onSuccess(body: Response<ResponseBody>)

        fun onError(code: Int, error: String?)
    }
}