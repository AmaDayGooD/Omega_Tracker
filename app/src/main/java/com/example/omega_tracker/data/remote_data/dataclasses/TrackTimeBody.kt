package com.example.omega_tracker.data.remote_data.dataclasses

import com.example.omega_tracker.entity.SendBody
import com.squareup.moshi.Json

data class TrackTimeBody(
    @Json(name = "duration")
    val duration: Duration,
    @Json(name = "text")
    val text: String,
    @Json(name = "date")
    override val date: Long,
    @Json(name = "stateTask")
    override val stateTask: String,
    @Json(name = "author")
    val author: Author
):SendBody{
    constructor(sendBody:SendBody):this(
        Duration(""),
        "",
        sendBody.date,
        "",
        Author("")
    )

    override val description: String
        get() = text

    override val timeSpent: String
        get() = duration.presentation

    override val authorId:String
        get() = author.id
}


data class Duration(
    @Json(name = "presentation")
    val presentation: String
)

data class Author(
    @Json(name = "id")
    val id: String
)
