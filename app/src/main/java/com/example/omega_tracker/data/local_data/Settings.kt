package com.example.omega_tracker.data.local_data

import android.content.Context
import android.util.Log
import com.example.omega_tracker.data.DataProfile
import com.example.omega_tracker.entity.Profile

// Класс который сохраняет данные в Shared
class Settings(context: Context) {
    companion object {
        private const val PATH_FOR_TOKEN = "AUTH_TOKEN"
        private const val KEY_FOR_TOKEN = "TOKEN"
        private const val KEY_FOR_TYPE_ENTER_TIME = "KEY_FOR_TYPE_ENTER_TIME"
        private const val KEY_FOR_PROFILE_NAME = "KEY_FOR_PROFILE_NAME"
        private const val KEY_FOR_PROFILE_ID = "KEY_FOR_PROFILE_ID"
        private const val KEY_FOR_PROFILE_EMAIL = "KEY_FOR_PROFILE_EMAIL"
        private const val KEY_FOR_PROFILE_AVATAR_URI = "KEY_FOR_PROFILE_AVATAR_URI"
        private const val KEY_FOR_CURRENT_DISPLAY_CHART = "KEY_FOR_CURRENT_DISPLAY_CHART"
    }

    private val sharedPreferences =
        context.getSharedPreferences(PATH_FOR_TOKEN, Context.MODE_PRIVATE)

    fun saveString(value: String) {
        sharedPreferences.edit().putString(KEY_FOR_TOKEN, value).apply()
    }
    fun test(){
        sharedPreferences
    }

    fun saveProfile(profile: Profile) {
        sharedPreferences.edit()
            .putString(KEY_FOR_PROFILE_ID, profile.id)
            .putString(KEY_FOR_PROFILE_NAME, profile.name)
            .putString(KEY_FOR_PROFILE_EMAIL, profile.email)
            .putString(KEY_FOR_PROFILE_AVATAR_URI, profile.avatar.toString())
            .apply()
    }

    fun getProfile(): Profile {
        return DataProfile(
            name = sharedPreferences.getString(KEY_FOR_PROFILE_NAME, null) ?: "",
            id = sharedPreferences.getString(KEY_FOR_PROFILE_ID, null) ?: "",
            email = sharedPreferences.getString(KEY_FOR_PROFILE_EMAIL, null) ?: "",
            avatarUrl = sharedPreferences.getString(KEY_FOR_PROFILE_AVATAR_URI, null) ?: "",
        )
    }

    fun deleteProfile() {
        sharedPreferences.edit()
            .remove(KEY_FOR_PROFILE_ID)
            .remove(KEY_FOR_PROFILE_NAME)
            .remove(KEY_FOR_PROFILE_EMAIL)
            .remove(KEY_FOR_PROFILE_AVATAR_URI)
            .apply()
    }

    fun changeTypeEnterTime(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FOR_TYPE_ENTER_TIME, value).apply()
    }

    fun getTypeEnterTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_FOR_TYPE_ENTER_TIME, false)
    }

    fun saveCurrentDisplay(value: Boolean){
        sharedPreferences.edit().putBoolean(KEY_FOR_CURRENT_DISPLAY_CHART, value).apply()
    }

    fun getCurrentDisplay():Boolean{
        return sharedPreferences.getBoolean(KEY_FOR_CURRENT_DISPLAY_CHART,false)
    }


    fun getToken(): String? {
        return sharedPreferences.getString(KEY_FOR_TOKEN, "")
    }

    fun deleteString() {
        sharedPreferences.edit().remove(KEY_FOR_TOKEN).apply()
    }
}