package com.example.omega_tracker.data.repository.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.ceil

@Entity(tableName = "Tasks")
data class NameEntity(
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
    var timeLeft:String     // Времени осталось
)
