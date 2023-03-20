package com.hashone.module.textview.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.model.DataResponse
import java.util.*

class ResourcesResponseData(private val parentActivity: Context) {
    private var pref: SharedPreferences? = null
    private val APP_KEY: String = "resources_response_${
        parentActivity.packageName.replace("\\.".toRegex(), "_").lowercase(Locale.getDefault())
    }"

    fun setString(key: String, value: String) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return pref!!.getString(key, "")
    }

    fun setDouble(key: String, value: Double) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putString(key, value.toString() + "")
        editor.apply()
    }

    fun getDouble(key: String): Double? {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return if (pref!!.getString(key, "")!!.isNotEmpty()) {
            java.lang.Double.parseDouble(pref!!.getString(key, "")!!)
        } else {
            null
        }
    }

    fun setBoolean(key: String, value: Boolean) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return pref!!.getBoolean(key, false)
    }

    fun setInt(key: String, value: Int) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return pref!!.getInt(key, -1)
    }

    fun setLong(key: String, value: Long) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String): Long {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return pref!!.getLong(key, 0)
    }

    fun is_exist(key: String): Boolean {
        pref = parentActivity.getSharedPreferences(
            APP_KEY, Context.MODE_PRIVATE
        )
        return pref!!.contains(key)
    }

    fun clearData(context: Context) {
        val settings = context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
        settings.edit().clear().apply()
    }

    fun deleteIfContains(prefKey: String = "") {
        if (prefKey.isNotEmpty()) {
            val allEntries: MutableMap<String, *>? = pref?.all
            if (allEntries != null) {
                for ((key, _) in allEntries.entries) {
                    if (key.startsWith(prefKey)) {
                        setString(key, "")
                    }
                }
            }
        }
    }

    fun getDataResponse(key: String): DataResponse? {
        val responseString = getString(key)
        return if (!responseString.isNullOrEmpty()) {
            Utils.getGson().fromJson(responseString, DataResponse::class.java)
        } else null
    }
}