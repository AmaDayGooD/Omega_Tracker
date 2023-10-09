package com.example.omega_tracker.ui.base_class

import android.content.Context

interface BaseInterfase{
    fun showToast(toastType: Int, message: Int)
    //fun getContext(): Context
    fun log(message:String)
}