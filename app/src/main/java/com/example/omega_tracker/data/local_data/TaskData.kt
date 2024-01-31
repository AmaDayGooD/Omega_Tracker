package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omega_tracker.data.TaskStatus
import java.time.LocalDateTime

@Entity(tableName = "Tasks")
data class TaskData(
    @PrimaryKey()
    var id_tasks:String,    // ID задачи
    var nameProject:String, // азвание проекта
    var summary:String,     // Название задачи
    var description:String, // Описание
    var currentTime:String, // Сколько прошло времени в данный момент
    var currentState:String,// Текущее состояние задачи
    val estimate:String,    // Оценка времени задачи
    val startDate:String,   // Дата и время начала задачи
    var timeSpent:String,   // Время прошло
    var timeLeft:String,     // Времени осталось
    var taskStatus: TaskStatus,
    var taskType: TaskType,
    var taskLaunchTime : String?
)

enum class TaskType{
    YouTrack,Custom
}
