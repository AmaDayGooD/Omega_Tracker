package com.example.omega_tracker.entity

import android.net.Uri

interface Profile {
    var name:String
    val id:String
    val email:String
    val avatar:Uri
}