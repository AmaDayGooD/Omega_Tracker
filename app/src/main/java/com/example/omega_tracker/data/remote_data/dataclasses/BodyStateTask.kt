package com.example.omega_tracker.data.remote_data.dataclasses

import com.squareup.moshi.Json

data class BodyStateTask(
    @Json(name = "value")
    val value:Id
)
data class Id(
    @Json(name = "id")
    val id: String
)