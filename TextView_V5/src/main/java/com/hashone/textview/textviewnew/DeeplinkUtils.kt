package com.hashone.allinone.utils

import java.net.URLDecoder

object DeeplinkUtils {

    const val TYPE_CATEGORIES = "categories"
    const val TYPE_SUB_CATEGORIES = "subcategories"
    const val TYPE_CONTENT_TAGS = "contenttags"
    const val TYPE_HOME_HEADER = "homeheader"

    const val CONTENT_URL_1 = "https://aio.app.link/d/" // VERIFIED
    const val CONTENT_URL_2 = "https://aio.app.link/" // VERIFIED
    const val CONTENT_URL_3 = "https://www.hashone.com/aio/" // VERIFIED
    const val CONTENT_URL_4 = "https://www.hashone.com/" // VERIFIED
    const val CONTENT_URL_5 = "https://aio1.page.link/"
    fun parseDeeplinkContents(deepLink: String): List<String> {
        //  graphicscategories/16
        val afterDecode: String =
            URLDecoder.decode(deepLink, "UTF-8")
        val trimmedString = when {
            afterDecode.startsWith(CONTENT_URL_1) -> {
                afterDecode.replace(CONTENT_URL_1, "")
            }

            afterDecode.startsWith(CONTENT_URL_2) -> {
                afterDecode.replace(CONTENT_URL_2, "")
            }

            afterDecode.startsWith(CONTENT_URL_3) -> {
                afterDecode.replace(CONTENT_URL_3, "")
            }

            afterDecode.startsWith(CONTENT_URL_4) -> {
                afterDecode.replace(CONTENT_URL_4, "")
            }

            afterDecode.startsWith(CONTENT_URL_5) -> {
                afterDecode.replace(CONTENT_URL_5, "")
            }

            else -> ""
        }.trim()
        return if (trimmedString.isNotEmpty()) {
            trimmedString.split("/")
        } else emptyList()
    }
}