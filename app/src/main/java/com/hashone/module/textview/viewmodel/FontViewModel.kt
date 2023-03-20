package com.hashone.module.textview.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.Constants.RESPONSE_FONTS
import com.hashone.commonutils.utils.Constants.RESPONSE_FONT_CATEGORIES
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.BuildConfig
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.model.DataResponse
import com.hashone.module.textview.retrofit.Resource
import com.hashone.module.textview.retrofit.RetrofitHelper
import com.hashone.module.textview.retrofit.repository.Api
import com.hashone.module.textview.retrofit.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
open class FontViewModel @Inject constructor(
    val apiRepository: ApiRepository, @ApplicationContext private val context: Context
) : ViewModel() {

    @Inject
    lateinit var api: Api

    var fontCategoriesResponse: MutableLiveData<Resource<DataResponse?>> = MutableLiveData()
    var fontsResponse: MutableLiveData<Resource<DataResponse?>> = MutableLiveData()

    var apiPageCount = 1
    var isLoadMoreEnabled = true
    var isCategoriesLoadMoreEnabled = true

    //TODO: Home Contents
    fun getFonts() = viewModelScope.launch {
        if (isLoadMoreEnabled) handleFontApiCall()
    }

    private suspend fun handleFontApiCall() {
        fontsResponse.postValue(Resource.Loading())
        try {
            if (Utils.isNetworkAvailable(context)) {
                val retrofitHelper = RetrofitHelper()
                val hashMap = retrofitHelper.getFieldMap()
                hashMap[Constants.PARAMS_LIMIT] = "${Constants.FONT_LIMIT}"
                hashMap[Constants.PARAMS_PAGE] = "$apiPageCount"
                hashMap[Constants.PARAMS_ORDER_BY] = Constants.VALUES_NAME
                hashMap[Constants.PARAMS_ORDER_BY_TYPE] = Constants.VALUES_ASC

                val jsonArray = JSONArray()

                hashMap[Constants.PARAMS_FILTER] = Constants.VALUES_ACTIVE
                jsonArray.put(
                    retrofitHelper.prepareCondition(
                        Constants.PARAMS_STATUS, "=", "1"
                    )
                )
                jsonArray.put(
                    retrofitHelper.prepareCondition(
                        Constants.PARAMS_SCHEDULED, "=", "0"
                    )
                )

                hashMap[Constants.PARAMS_WHERE] = jsonArray.toString()
                val response = apiRepository.getFonts(hashMap)
                fontsResponse.postValue(handleFontResponse(response))
            } else {
                fontsResponse.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is IOException -> fontsResponse.postValue(Resource.Error("Network Failure"))
                else -> fontsResponse.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleFontResponse(response: Response<ResponseBody>): Resource<DataResponse?> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                val responseString = resultResponse.string()
                resultResponse.close()
                val dataResponse =
                    Utils.getGson().fromJson(responseString, DataResponse::class.java)
                if (dataResponse.status) {
                    isLoadMoreEnabled = false
                    MyApplication.instance.resourcesResponseData.setString(RESPONSE_FONTS, responseString)
                    return Resource.Success(dataResponse)
                } else {
                    return Resource.Success(null)
                }
            }
        } else {
            response.errorBody()?.close()
        }
        return Resource.Error(response.message())
    }

    fun getFontCategories() = viewModelScope.launch {
        if (isCategoriesLoadMoreEnabled) handleFontCategoriesApiCall()
    }

    private suspend fun handleFontCategoriesApiCall() {
        fontCategoriesResponse.postValue(Resource.Loading())
        try {
            if (Utils.isNetworkAvailable(context)) {
                val retrofitHelper = RetrofitHelper()
                val hashMap = retrofitHelper.getFieldMap()
                hashMap[Constants.PARAMS_LIMIT] = "${Constants.FONT_LIMIT}"
                hashMap[Constants.PARAMS_PAGE] = "$apiPageCount"
                hashMap[Constants.PARAMS_ORDER_BY] = Constants.VALUES_SORT
                hashMap[Constants.PARAMS_ORDER_BY_TYPE] = Constants.VALUES_ASC

                val jsonArray = JSONArray()

                hashMap[Constants.PARAMS_FILTER] = Constants.VALUES_ACTIVE

                jsonArray.put(
                    retrofitHelper.prepareCondition(
                        Constants.PARAMS_STATUS, "=", "1"
                    )
                )
                jsonArray.put(
                    retrofitHelper.prepareCondition(
                        Constants.PARAMS_SCHEDULED, "=", "0"
                    )
                )

                hashMap[Constants.PARAMS_WHERE] = jsonArray.toString()
                val response = apiRepository.getFontCategories(hashMap)
                fontCategoriesResponse.postValue(handleFontCategoriesResponse(response))
            } else {
                fontCategoriesResponse.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is IOException -> fontCategoriesResponse.postValue(Resource.Error("Network Failure"))
                else -> fontCategoriesResponse.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleFontCategoriesResponse(response: Response<ResponseBody>): Resource<DataResponse?> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                val responseString = resultResponse.string()
                resultResponse.close()
                if (!responseString.isNullOrEmpty()) {
                    val dataResponse =
                        Utils.getGson().fromJson(responseString, DataResponse::class.java)
                    return if (dataResponse.status) {
                        isCategoriesLoadMoreEnabled = false
                        MyApplication.instance.resourcesResponseData.setString(RESPONSE_FONT_CATEGORIES, responseString)
                        Resource.Success(dataResponse)
                    } else {
                        Resource.Success(null)
                    }
                }
            }
        } else {
            response.errorBody()?.close()
        }
        return Resource.Error(response.message())
    }
}
