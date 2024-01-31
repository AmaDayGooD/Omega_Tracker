package com.example.omega_tracker.di

import android.content.Context
import com.example.omega_tracker.data.local_data.TasksDB
import com.example.omega_tracker.data.local_data.TasksDao
import dagger.Module
import dagger.Provides

@Module(includes = [ContextModule::class])
class DataBase {
    @Provides
    fun provideDataBase(context: Context): TasksDao {
        return TasksDB.createDataBase(context).dao
    }
}