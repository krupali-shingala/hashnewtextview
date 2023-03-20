package com.hashone.module.textview.retrofit.repository

import com.hashone.commonutils.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class ApiRepository @Inject constructor(private val api: Api) {

    open suspend fun getHome(
        path: String, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas(path, hashMap)
    }

    open suspend fun getBackgroundsCategories(
        page: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas(Constants.BACKGROUND_CATEGORIES_API, hashMap)
    }

    open suspend fun getBackgroundsByCategoryId(
        page: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas(Constants.BACKGROUNDS, hashMap)
    }

    open suspend fun getExtra(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.MODULES_CONTENTS_API, hashMap)
    }

    open suspend fun getFonts(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.FONT_API, hashMap)
    }

    open suspend fun getFontCategories(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.FONT_CATEGORIES_API, hashMap)
    }

    open suspend fun getGraphicCategory(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.GRAPHIC_CATEGORY_API, hashMap)
    }

    open suspend fun getGraphics(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.GRAPHIC_API, hashMap)
    }

    open suspend fun searchGraphics(searchKeyWord: String, hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.SEARCH_GRAPHICS_API.trim() + searchKeyWord, hashMap)
    }

    open suspend fun getSearchData(
        searchKeyWord: String, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas(Constants.SEARCH_API.trim() + searchKeyWord, hashMap)
    }

    open suspend fun updateContentDownloadCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.CONTENT_DOWNLOAD_API.trim()}$contentId/zip_file", hashMap)
    }

    open suspend fun updateBackgroundDownloadCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.BACKGROUND_DOWNLOAD_API.trim()}$contentId/preview_image", hashMap)
    }

    open suspend fun updateFilterDownloadCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.FILTER_DOWNLOAD_API.trim()}$contentId/filter_file", hashMap)
    }

    open suspend fun updateFontDownloadCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.FONT_DOWNLOAD_API.trim()}$contentId/font_file", hashMap)
    }

    open suspend fun updateGraphicDownloadCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.GRAPHIC_DOWNLOAD_API.trim()}$contentId/preview_image", hashMap)
    }

    open suspend fun updateContentViewCount(
        contentId: Int, hashMap: HashMap<String, String>
    ): Response<ResponseBody> {
        return api.getDatas("${Constants.CONTENT_VIEW_API.trim()}$contentId", hashMap)
    }

    open suspend fun installAPI(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.INSTALL_API.trim(), hashMap)
    }

    open suspend fun filtersAPI(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.FILTER_API.trim(), hashMap)
    }

    open suspend fun lastOpenedAtAPI(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.USER_DETAILS_API.trim(), hashMap)
    }
    open suspend fun syncUserDetailsAPI(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.USER_DETAILS_API.trim(), hashMap)
    }

    open suspend fun getTagsData(hashMap: HashMap<String, String>): Response<ResponseBody> {
        return api.getDatas(Constants.ACTIVE_CONTENT_TAGS_API.trim(), hashMap)
    }
}