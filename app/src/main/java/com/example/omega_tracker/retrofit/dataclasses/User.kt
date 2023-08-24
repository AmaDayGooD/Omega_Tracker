package com.example.omega_tracker.retrofit.dataclasses

import com.google.gson.annotations.SerializedName

data class User(
    val name: String,
    @SerializedName("\$type")
    val type: String
)
