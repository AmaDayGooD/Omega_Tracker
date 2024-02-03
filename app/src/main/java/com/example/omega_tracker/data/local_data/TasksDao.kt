package com.example.omega_tracker.data.local_data

import androidx.room.*
import kotlin.time.Duration

@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(entity: TaskData)

    @Query("UPDATE Tasks SET timeSpent= timeSpent+ :timeSpent, timeLeft = timeLeft- :timeSpent WHERE id_tasks = :idTask")
    suspend fun updateTimeCustomTask(timeSpent: String, idTask: String)

    @Update
    suspend fun updateCustomTask(entity: TaskData)

    @Query("SELECT * FROM Tasks WHERE taskStatus = 'Run' AND taskLaunchTime IS NOT NULL AND taskLaunchTime!='null'")
    suspend fun getTaskForRestore(): MutableList<TaskData>

    @Query("SELECT nameProject FROM Tasks Group by nameProject HAVING nameProject!='Личные задачи'")
    suspend fun getAllNameProjects(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedTask(statisticsData: StatisticsData)


    @Query("SELECT * FROM Tasks")
    suspend fun getAllTasks(): MutableList<TaskData>

    @Query("SELECT * FROM Tasks WHERE id_tasks = :idTask")
    suspend fun getTasksById(idTask: String): TaskData

    @Query("UPDATE Tasks SET taskStatus= :newStatus WHERE id_tasks = :idTask")
    suspend fun updateTaskStatus(newStatus: String, idTask: String)

    @Query("UPDATE Tasks SET taskLaunchTime = :timeNow WHERE id_tasks = :idTask")
    suspend fun updateTimeLaunch(timeNow: String, idTask: String)

    @Query("UPDATE Tasks set taskLaunchTime = 'null' where id_tasks=:idTask")
    suspend fun removeTaskLaunchTime(idTask: String)

    @Query("DELETE FROM Tasks WHERE id_tasks = :idTask AND taskType = \"Custom\"")
    suspend fun removeTask(idTask: String)

    @Query("DELETE FROM Tasks")
    suspend fun clearDataBase()
}