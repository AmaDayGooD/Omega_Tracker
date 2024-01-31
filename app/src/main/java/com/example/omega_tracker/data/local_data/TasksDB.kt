package com.example.omega_tracker.data.local_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskData::class,ProfileData::class], version = 1)
abstract class TasksDB : RoomDatabase() {
    abstract val dao: TasksDao
    companion object{
        fun createDataBase(context: Context): TasksDB {
            return Room.databaseBuilder(context, TasksDB::class.java,"tasks.db").build()
        }
    }
}