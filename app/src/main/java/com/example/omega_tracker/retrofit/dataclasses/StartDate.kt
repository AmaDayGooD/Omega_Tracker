package com.example.omega_tracker.retrofit.dataclasses

import com.google.gson.annotations.SerializedName

data class StartDate(
    val value: Long,
    val name : String,
    val id: String,
    @SerializedName("\$type")
    val type: String
)
