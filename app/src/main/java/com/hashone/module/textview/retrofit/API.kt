package com.hashone.module.textview.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface API {

    @FormUrlEncoded
    @POST("{path}")
    fun getData(
        @Path("path") path: String,
        @FieldMap hashMap: HashMap<String, String>
    ): Call<ResponseBody>

    @GET
    fun dynamicUrl(@Url url: String): Call<ResponseBody>

    @Streaming
    @GET
    fun downloadFileByUrl(@Url fileUrl: String): Call<ResponseBody>

    @Multipart
    @POST("{path}")
    fun uploadFile(
        @Path("path") path: String,
        @Part filePath: MultipartBody.Part,
//        @Part thumbPath: MultipartBody.Part,
        @PartMap partMap: HashMap<String, @JvmSuppressWildcards RequestBody>
    ): Call<ResponseBody>
}