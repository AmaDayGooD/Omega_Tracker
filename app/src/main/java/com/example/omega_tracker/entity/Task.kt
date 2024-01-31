package com.example.omega_tracker.entity

import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.TaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import kotlin.time.Duration

interface Task {
    var summary: String
    var description: String
    var id: String
    var nameProject: String
    var runningTime: Duration
    var currentState: String
    var evaluate: Duration
    var onset: LocalDateTime?
    var usedTime: Duration
    var remainingTime: Duration
    var taskStatus: TaskStatus
    val taskLaunchTime: LocalDateTime?
    val taskType: TaskType
}