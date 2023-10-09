package com.example.omega_tracker.data.repository.local_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NameEntity::class], version = 1)
abstract class TasksDB : RoomDatabase() {
    abstract val dao: Tasks_DAO
    companion object{
        fun createDataBase(context: Context): TasksDB{
            return Room.databaseBuilder(context,TasksDB::class.java,"tasks.db").build()
        }
    }
}