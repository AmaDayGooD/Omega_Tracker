package com.example.omega_tracker.data.repository.local_data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Tasks_DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun instertItem(entity: NameEntity)

    @Query("SELECT * FROM Tasks")
    fun getAllTasks(): MutableList<NameEntity>

    @Query("SELECT * FROM Tasks WHERE id_tasks = :idTask")
    fun getTasksById(idTask:String): MutableList<NameEntity>

}