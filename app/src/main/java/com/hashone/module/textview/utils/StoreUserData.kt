package com.hashone.module.textview.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.hashone.commonutils.utils.Utils
import java.util.*

class StoreUserData {
    private var pref: SharedPreferences? = null
    private var APP_KEY: String = ""

    lateinit var parentActivity: Context

    constructor(parentActivity: Context) {
        this.parentActivity = parentActivity
        APP_KEY = parentActivity.packageName.replace("\\.".toRegex(), "_")
            .lowercase(Locale.getDefault())
    }

    constructor(parentActivity: Context, response: String) {
        this.parentActivity = parentActivity
        APP_KEY =
            "${response}_${
                parentActivity.packageName.replace("\\.".toRegex(), "_")
                    .lowercase(Locale.getDefault())
            }"
    }

    fun setString(key: String, value: String) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return pref!!.getString(key, "")
    }

    fun setDouble(key: String, value: Double) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putString(key, value.toString() + "")
        editor.apply()
    }

    fun getDouble(key: String): Double? {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return if (pref!!.getString(key, "")!!.isNotEmpty()) {
            java.lang.Double.parseDouble(pref!!.getString(key, "")!!)
        } else {
            null
        }
    }

    fun setBoolean(key: String, value: Boolean) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return pref!!.getBoolean(key, false)
    }

    fun setInt(key: String, value: Int) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return pref!!.getInt(key, -1)
    }

    fun setLong(key: String, value: Long) {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        val editor = pref!!.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String): Long {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return pref!!.getLong(key, 0)
    }

    fun is_exist(key: String): Boolean {
        pref = parentActivity.getSharedPreferences(
            APP_KEY,
            Context.MODE_PRIVATE
        )
        return pref!!.contains(key)
    }

    fun clearData(context: Context) {
        val settings = context.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
        settings.edit().clear().apply()
    }

    fun getAllKeys(): MutableMap<String, *>? {
        return pref?.all
    }

    val KEY_DOWNLOADED_CONTENTS = "key_downloaded_contents"
    fun updateDownloadedContentIds(contentId: Int) {
        val contentIdsList = getDownloadedContentIds()
        if (!contentIdsList.contains(contentId)) {
            contentIdsList.add(contentId)
            setString(KEY_DOWNLOADED_CONTENTS, Utils.getGson().toJson(contentIdsList))
        }
    }

    fun getDownloadedContentIds(): ArrayList<Int> {
        val contentIds = getString(KEY_DOWNLOADED_CONTENTS)
        if (contentIds != null && contentIds.isNotEmpty()) {
            return Utils.getGson().fromJson(
                contentIds,
                object : TypeToken<ArrayList<Int>>() {}.type
            )
        }
        return arrayListOf()
    }

    fun isExistInDownloaded(contentId: Int): Boolean {
        return getDownloadedContentIds().contains(contentId)
    }

    val KEY_UNLOCKED_CONTENTS = "key_unlocked_contents"
    fun updateUnlockedContentIds(contentId: Int) {
        val contentIdsList = getUnlockedContentIds()
        if (!contentIdsList.contains(contentId)) {
            contentIdsList.add(contentId)
            setString(KEY_UNLOCKED_CONTENTS, Utils.getGson().toJson(contentIdsList))
        }
    }

    fun getUnlockedContentIds(): ArrayList<Int> {
        val contentIds = getString(KEY_UNLOCKED_CONTENTS)
        if (contentIds != null && contentIds.isNotEmpty()) {
            return Utils.getGson().fromJson(
                contentIds,
                object : TypeToken<ArrayList<Int>>() {}.type
            )
        }
        return arrayListOf()
    }

    fun isExistInUnlocked(contentId: Int): Boolean {
        return getUnlockedContentIds().contains(contentId)
    }
}