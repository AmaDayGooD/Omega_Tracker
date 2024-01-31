package com.example.omega_tracker.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ParseTaskLaunchTime {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun parseTaskLaunchTime(taskLaunchTime:String?): LocalDateTime?{
        return if(taskLaunchTime.isNullOrEmpty() || taskLaunchTime=="null")
            null
        else
            LocalDateTime.parse(taskLaunchTime,formatter)
    }
}