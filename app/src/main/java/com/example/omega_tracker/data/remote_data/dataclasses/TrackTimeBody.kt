package com.example.omega_tracker.data.remote_data.dataclasses

import com.squareup.moshi.Json


data class TrackTimeBody(
    @Json(name = "duration")
    val duration: Duration,
    @Json(name = "text")
    val text:String,
    @Json(name = "date")
    val date: Long,
    @Json(name = "stateTask")
    val stateTask:String,
    @Json(name = "author")
    val author: Author
)

data class Duration(
    @Json(name = "presentation")
    val presentation: String
)
data class Author(
    @Json(name = "id")
    val id:String
)
