package com.example.omega_tracker.data.local_data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Statistics")
data class StatisticsLocalData(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val idTask:String,
    val nameTask:String,
    val spentTime:String,
    val dataCompleted:String
)
