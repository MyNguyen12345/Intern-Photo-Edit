package com.example.photoedit.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.photoedit.constants.Constants

class SharedPreferencesUtils(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun isBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
}
