package com.hashone.module.textview.retrofit.repository

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @FormUrlEncoded
    @POST("{path}")
    suspend fun getDatas(
        @Path(value = "path", encoded = true) path: String,
        @FieldMap hashMap: HashMap<String, String>
    ): Response<ResponseBody>

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileUrl: String): Response<ResponseBody>

    @GET
    suspend fun dynamicUrl(@Url url: String): Result<ResponseBody>

    @GET("{path}")
    suspend fun getData(@Path("path") path: String): Response<ResponseBody>
}