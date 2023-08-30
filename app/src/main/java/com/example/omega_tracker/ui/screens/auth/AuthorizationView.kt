package com.example.omega_tracker.ui.screens.auth

import android.content.Context

interface AuthorizationView {
    fun showToast(message: String)
    fun gotoNextActivity(context:Context,token: String)

}