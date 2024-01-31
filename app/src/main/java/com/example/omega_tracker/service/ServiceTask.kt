package com.example.omega_tracker.service

import com.example.omega_tracker.data.TaskStatus
import kotlin.time.Duration

data class ServiceTask(
    val id:String,
    val title:String,
    val timeLeft: Duration,
    val timeSpent:Duration,
    val timeFromLaunch:Duration,
    val taskStatus: TaskStatus
)
