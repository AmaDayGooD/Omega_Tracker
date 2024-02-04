package com.example.omega_tracker.entity

import java.time.LocalDateTime
import kotlin.time.Duration

interface Statistics {
    val idTask:String
    val nameTask:String
    val duration:Duration
    val dataTimeCompleted:LocalDateTime
}