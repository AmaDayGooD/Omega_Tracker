package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.remote_data.dataclasses.Author
import org.w3c.dom.Comment

@Entity(tableName = "Pending")
data class PendingTaskData(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val idTask:String,
    val duration: String,
    val comment: String,
    val date: Long,
    val stateTask: String,
    val author: String
)