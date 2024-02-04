package com.example.omega_tracker.data

import com.example.omega_tracker.entity.Statistics
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class DataStatistics(
    override val idTask: String,
    override val nameTask: String,
    val spentTime: String,
    val dataCompleted: String
) : Statistics {
    constructor(statistics: Statistics) : this(
        statistics.idTask,
        statistics.nameTask,
        "",
        ""
    )

    override val duration: Duration
        get() = Duration.parse(spentTime)

    override val dataTimeCompleted: LocalDateTime
        get() = LocalDateTime.parse(dataCompleted)


}