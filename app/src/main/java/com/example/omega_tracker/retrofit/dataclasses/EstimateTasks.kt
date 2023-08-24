package com.example.omega_tracker.retrofit.dataclasses

import com.google.gson.annotations.SerializedName

data class EstimateTasks(
    val value: Value,
    val name : String,
    val summary: String,
    val id: String,
    @SerializedName("\$type")
    val type: String
)

data class Value(
    val minutes:Long,
    @SerializedName("\$type")
    val type: String
)
