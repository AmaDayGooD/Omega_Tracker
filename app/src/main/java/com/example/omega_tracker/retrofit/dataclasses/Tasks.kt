package com.example.omega_tracker.retrofit.dataclasses

import com.google.gson.annotations.SerializedName

data class Tasks(
    val summary : String,
    val description : String,
    val id : String,
    @SerializedName("\$type")
    val type: String
)
