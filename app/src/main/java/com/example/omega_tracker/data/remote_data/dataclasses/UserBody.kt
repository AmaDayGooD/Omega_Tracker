package com.example.omega_tracker.data.remote_data.dataclasses

import com.squareup.moshi.Json

data class UserBody(
    @Json(name = "name")
    val name: String,
    @Json(name = "avatarUrl")
    val avatarUrl: String,
    @Json(name = "email")
    val email:String,
    @Json(name = "id")
    val id:String
)
