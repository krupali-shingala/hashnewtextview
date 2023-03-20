package com.hashone.module.textview.retrofit.repository

import com.hashone.module.textview.retrofit.API1
import com.hashone.commonutils.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CountryApiRepository @Inject constructor(private val api: API1) {
    open suspend fun getCountryDetails(): Response<ResponseBody> {
        return api.getData(Constants.COUNTRY_DETAILS_API)
    }
}