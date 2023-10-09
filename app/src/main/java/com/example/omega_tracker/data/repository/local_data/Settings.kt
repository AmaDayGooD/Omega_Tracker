package com.example.omega_tracker.data.repository.local_data

import android.content.Context

// Класс который сохраняет данные в Shared
class Settings (context: Context) {
    companion object{
        private const val PATH_FOR_TOKEN = "AUTH_TOKEN"
        private const val KEY_FOR_TOKEN = "TOKEN"
    }

    private val sharedPreferences = context.getSharedPreferences(PATH_FOR_TOKEN, Context.MODE_PRIVATE)

    fun saveString(value: String) {
        sharedPreferences.edit().putString(KEY_FOR_TOKEN,value).apply()
    }

    fun getString(): String? {
        return sharedPreferences.getString(KEY_FOR_TOKEN, "")
    }

    fun deleteString(){
        sharedPreferences.edit().remove(KEY_FOR_TOKEN).apply()
    }
}