package com.hashone.module.textview.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashone.module.textview.retrofit.Resource
import com.hashone.module.textview.retrofit.RetrofitHelper
import com.hashone.module.textview.retrofit.repository.Api
import com.hashone.module.textview.retrofit.repository.ApiRepository
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.model.DataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
open class HomeViewModel @Inject constructor(
    val apiRepository: ApiRepository, @ApplicationContext private val context: Context
) : ViewModel() {

    @Inject
    lateinit var api: Api
    var backgroundCategoriesResponse: MutableLiveData<Resource<DataResponse?>> = MutableLiveData()
    var backgroundsResponse: MutableLiveData<Resource<DataResponse?>> = MutableLiveData()

    var homeApiPageCount = 1
    var moreDataAvailable = true

    //TODO: Background Category Contents
    fun getBackgroundCategories() = viewModelScope.launch {
        if (MyApplication.instance.resourcesResponseData.getString(Constants.RESPONSE_BACKGROUND_CATEGORIES)!!
                .isEmpty()
        ) handleBackgroundCategoriesApiCall()
    }

    private suspend fun handleBackgroundCategoriesApiCall() {
        backgroundCategoriesResponse.postValue(Resource.Loading())
        try {
            if (Utils.isNetworkAvailable(context)) {
                val retrofitHelper = RetrofitHelper()

                val hashMap = retrofitHelper.getFieldMap()
                hashMap[Constants.PARAMS_PAGE] = "1"
                hashMap[Constants.PARAMS_LIMIT] = "200"
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

                val response = apiRepository.getBackgroundsCategories(homeApiPageCount, hashMap)
                backgroundCategoriesResponse.postValue(handleBackgroundCategoriesResponse(response))
            } else {
                backgroundCategoriesResponse.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is IOException -> backgroundCategoriesResponse.postValue(Resource.Error("Network Failure"))
                else -> backgroundCategoriesResponse.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleBackgroundCategoriesResponse(response: Response<ResponseBody>): Resource<DataResponse?> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                val responseString = resultResponse.string()
                resultResponse.close()
                val dataResponse =
                    Utils.getGson().fromJson(responseString, DataResponse::class.java)
                if (dataResponse.status) {
                    MyApplication.instance.resourcesResponseData.setString(
                        Constants.RESPONSE_BACKGROUND_CATEGORIES, responseString
                    )

                    if (dataResponse.data.isNotEmpty()) {
                        // totalCount = dataResponse.count
                        return Resource.Success(dataResponse)
                    } else {
                        moreDataAvailable = false
                        return Resource.Success(null)
                    }
                } else {
                    moreDataAvailable = false
                    return Resource.Success(null)
                }
            }
        } else {
            response.errorBody()?.close()
        }
        return Resource.Error(response.message())
    }

    //TODO: Background Contents
    fun getBackgroundContents(categoryId: Int) = viewModelScope.launch {
        if (MyApplication.instance.resourcesResponseData.getString("${Constants.RESPONSE_BACKGROUND_CONTENTS_BY_}$categoryId")!!
                .isEmpty()
        ) handleBackgroundContentsApiCall(categoryId)
    }

    private suspend fun handleBackgroundContentsApiCall(categoryId: Int) {
        backgroundsResponse.postValue(Resource.Loading())
        try {
            if (Utils.isNetworkAvailable(context)) {
                val retrofitHelper = RetrofitHelper()

                val hashMap = retrofitHelper.getFieldMap()
                hashMap[Constants.PARAMS_PAGE] = "1"
                hashMap[Constants.PARAMS_LIMIT] = "1000"
//                hashMap[Constants.PARAMS_ORDER_BY] = Constants.VALUES_CREATED_AT
//                hashMap[Constants.PARAMS_ORDER_BY_TYPE] = Constants.VALUES_DESC
                hashMap[Constants.PARAMS_WITH] = Constants.VALUES_BACKGROUND_CATEGORIES

                val orderByJsonArray = JSONArray()
                orderByJsonArray.put(
                    retrofitHelper.orderByCondition(
                        Constants.VALUES_BY_FREE, Constants.VALUES_DESC
                    )
                )
                orderByJsonArray.put(
                    retrofitHelper.orderByCondition(
                        Constants.VALUES_CREATED_AT, Constants.VALUES_DESC
                    )
                )
                hashMap[Constants.PARAMS_ORDER_BY_ARRAY] = orderByJsonArray.toString()

                val jsonArray = JSONArray()

                jsonArray.put(
                    retrofitHelper.prepareCondition(
                        Constants.PARAMS_PRIMARY_BACKGROUND_CATEGORY_ID, "=", "$categoryId"
                    )
                )

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

                val response = apiRepository.getBackgroundsByCategoryId(homeApiPageCount, hashMap)
                backgroundsResponse.postValue(
                    handleBackgroundContentsResponse(
                        categoryId, response
                    )
                )
            } else {
                backgroundsResponse.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is IOException -> backgroundsResponse.postValue(Resource.Error("Network Failure"))
                else -> backgroundsResponse.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleBackgroundContentsResponse(
        categoryId: Int, response: Response<ResponseBody>
    ): Resource<DataResponse?> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                val responseString = resultResponse.string()
                resultResponse.close()
                val dataResponse =
                    Utils.getGson().fromJson(responseString, DataResponse::class.java)
                if (dataResponse.status) {
                    MyApplication.instance.resourcesResponseData.setString(
                        "${Constants.RESPONSE_BACKGROUND_CONTENTS_BY_}$categoryId", responseString
                    )
                    if (dataResponse.data.isNotEmpty()) {
                        return Resource.Success(dataResponse)
                    } else {
                        moreDataAvailable = false
                        return Resource.Success(null)
                    }
                } else {
                    moreDataAvailable = false
                    return Resource.Success(null)
                }
            }
        } else {
            response.errorBody()?.close()
        }
        return Resource.Error(response.message())
    }
}
