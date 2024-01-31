package com.example.omega_tracker.ui.screens.main

import java.time.LocalDateTime
import kotlin.time.Duration

data class CustomTask(
    val idTask:String?,
    val nameProject:String?,
    val summary:String?,
    val description:String?,
    val estimate:Duration?,
    val startDateTime: Long,
    val timeLeft:Duration?,
    val taskLaunchTime: LocalDateTime?
)
