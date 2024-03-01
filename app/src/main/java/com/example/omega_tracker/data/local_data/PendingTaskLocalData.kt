package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pending")
data class PendingTaskLocalData(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val idTask:String,
    val duration: String,
    val comment: String,
    val date: Long,
    val stateTask: String,
    val author: String
)