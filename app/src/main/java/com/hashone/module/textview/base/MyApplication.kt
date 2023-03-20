package com.hashone.module.textview.base

import android.content.Context
import android.os.Build
import android.webkit.WebView
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.hashone.commonutils.checkinternet.config.NetworkConfig
import com.hashone.commonutils.utils.Constants.ANDROID_DEVICE_TOKEN
import com.hashone.module.textview.utils.AndroidDeviceIdentifier
import com.hashone.module.textview.utils.ResourcesResponseData
import com.hashone.module.textview.utils.StoreUserData
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : MultiDexApplication() {

//    init {
//        System.loadLibrary("native-lib")
//    }

    lateinit var storeUserData: StoreUserData
    lateinit var resourcesResponseData: ResourcesResponseData

    companion object {
        lateinit var instance: MyApplication
    }

    var context: Context? = null
    var isSyncInProgress: Boolean = false

    fun setMContext(context: Context) {
        this.context = context
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            storeUserData = StoreUserData(base)
            resourcesResponseData = ResourcesResponseData(base)
            super.attachBaseContext(base)
            MultiDex.install(base)
        } else {
            super.attachBaseContext(base)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        SeparatedTask().execute()

        NetworkConfig.initNetworkConfig(this)
    }

    private inner class SeparatedTask : CoroutineAsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            storeUserData.setString(ANDROID_DEVICE_TOKEN, AndroidDeviceIdentifier.getUniqueDeviceIdentifier(instance))
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val process = getProcessName()
                if (packageName != process) WebView.setDataDirectorySuffix(process)
            }
        }
    }

    fun isPremiumVersion(): Boolean {
//        return if (!BuildConfig.isInactive) {
//            storeUserData.getBoolean(Constants.KEY_IS_PREMIUM_PURCHASED)
//        } else {
//            true
//        }
        return true
    }

    fun freeMemory() {
        try {
            System.runFinalization()
            Runtime.getRuntime().gc()
            System.gc()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}