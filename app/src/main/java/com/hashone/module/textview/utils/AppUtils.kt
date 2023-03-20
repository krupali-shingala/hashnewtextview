package com.hashone.module.textview.utils

import android.content.Context
import android.net.ConnectivityManager
import com.hashone.module.textview.BuildConfig

object AppUtils {

    fun getAppPackageName(): String {
        return BuildConfig.APPLICATION_ID
    }

}