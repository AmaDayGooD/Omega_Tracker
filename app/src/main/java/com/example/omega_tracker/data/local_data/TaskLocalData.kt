package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omega_tracker.data.TaskStatus

@Entity(tableName = "Tasks")
data class TaskLocalData(
    @PrimaryKey()
    var id_tasks: String,    // ID task
    var nameProject: String, // Name project
    var iconUrl: String?,
    var summary: String,     // Name task
    var description: String, // Description
    var currentState: String,// Current state task
    val estimate: String,    // Оценка времени задачи
    val startDate: String,   // Дата и время начала задачи
    var timeSpent: String,   // Время прошло
    var timeLeft: String,     // Времени осталось
    var taskStatus: TaskStatus,
    var taskType: TaskType,
    var taskLaunchTime: String?
)

enum class TaskType {
    YouTrack, Custom
}
