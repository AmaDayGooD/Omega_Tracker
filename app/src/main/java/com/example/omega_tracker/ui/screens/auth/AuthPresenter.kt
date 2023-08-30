package com.example.omega_tracker.ui.screens.auth

import android.content.Context
import android.content.Context.MODE_PRIVATE

class AuthPresenter {

    fun correctToken(token:String): Boolean {
        return token.isEmpty()
    }

    fun checkToken(token: String): String{
        return if(token.contains("perm:"))
            "Bearer $token"
        else "Bearer perm:$token"
    }

    fun saveToken(context: Context, token: String){
        val sharedPref = context.getSharedPreferences("token", MODE_PRIVATE)
        with(sharedPref.edit()){
            putString("token", token)
            apply()
        }

    }

}