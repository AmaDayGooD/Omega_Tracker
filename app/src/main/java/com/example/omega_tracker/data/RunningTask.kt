package com.example.omega_tracker.data

import android.net.Uri

data class RunningTask(
    val id: String,
    val summary: String,
    var timeLeft: String,
    val taskStatus: String
)
