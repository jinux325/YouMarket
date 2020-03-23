package com.u.marketapp.utils

import android.content.Context
import android.preference.PreferenceManager
import org.json.JSONArray

class SharedPreferencesUtils {

    companion object {
        private val TAG = SharedPreferencesUtils::class.java.simpleName
        val instance = SharedPreferencesUtils()
    }

    // Array -> JSON
    fun setStringArrayPref(context: Context, key: String, values: ArrayList<String>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        val a = JSONArray()

        for (value in values) {
            a.put(value)
        }

        if (values.isEmpty()) {
            editor.putString(key, null)
        } else {
            editor.putString(key, a.toString())
        }

        editor.apply()
    }

    // JSON -> Array
    fun getStringArrayPref(context: Context, key: String) : ArrayList<String> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val json = prefs.getString(key, null)
        val urls = ArrayList<String>()

        if (json != null) {
            val a = JSONArray(json)
            for (i in 0 .. a.length()) {
                val url = a.optString(i)
                urls.add(url)
            }
        }
        return urls
    }

}