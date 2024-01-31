package com.example.omega_tracker.data.local_data

import android.util.Log
import com.example.omega_tracker.data.DataProfile
import com.example.omega_tracker.data.AppDataTask
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.ui.screens.main.CustomTask
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class GetDataFromBd @Inject constructor(
    private val retrofit: Retrofit,
    private val dataBaseTasks: TasksDao
) {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun getTaskForRestore(): MutableList<Task> {
        return convertingDataListFromNameEntity(dataBaseTasks.getTaskForRestore())
    }

    suspend fun getAllTaskBd(): MutableList<Task> {
        return convertingDataListFromNameEntity(dataBaseTasks.getAllTasks())
    }

    suspend fun getTaskById(id: String): AppDataTask {
        return convertingOneItemFromNameEntity(dataBaseTasks.getTasksById(id))
    }

    suspend fun updateTaskStatus(newStatus: TaskStatus, idTask: String) {
        dataBaseTasks.updateTaskStatus(newStatus.toString(), idTask)
    }

    suspend fun createCustomTask(taskData: TaskData) {
        dataBaseTasks.insertTask(taskData)
    }

    suspend fun getAllNameProjects(): List<String> {
        return dataBaseTasks.getAllNameProjects()
    }

    suspend fun clearDataBase() {
        dataBaseTasks.clearDataBase()
    }

    suspend fun updateTimeCustomTask(timeSpent: String, idTask: String) {
        dataBaseTasks.updateTimeCustomTask(timeSpent, idTask)
    }

    suspend fun updateCustomTask(task: TaskData) {
        dataBaseTasks.updateCustomTask(task)
    }

    suspend fun removeTaskLaunchTime(idTask: String) {
        dataBaseTasks.removeTaskLaunchTime(idTask)
    }

    suspend fun removeTask(idTask: String) {
        dataBaseTasks.removeTask(idTask)
    }

    private fun convertingOneItemFromNameEntity(result: TaskData): AppDataTask {
        return AppDataTask(
            id = result.id_tasks,
            nameProject = result.nameProject,
            summary = result.summary,
            description = result.description,
            currentTime = result.currentTime.toInt().seconds,
            currentState = result.currentState,
            estimate = result.estimate.toDouble().seconds,
            startDate = LocalDateTime.ofEpochSecond(result.startDate.toLong(), 0, ZoneOffset.UTC),
            timeSpent = result.timeSpent.toDouble().seconds,
            timeLeft = result.timeLeft.toDouble().seconds,
            taskStatus = result.taskStatus,
            taskLaunchTime = parseTaskLaunchTime(result.taskLaunchTime),
            taskType = result.taskType
        )
    }

    private fun convertingDataListFromNameEntity(result: MutableList<TaskData>): MutableList<Task> {
        val appTaskList = mutableListOf<Task>()
        result.forEach {
            appTaskList.add(
                AppDataTask(
                    id = it.id_tasks,
                    nameProject = it.nameProject,
                    summary = it.summary,
                    description = it.description,
                    currentTime = it.currentTime.toDouble().seconds,
                    currentState = it.currentState,
                    estimate = it.estimate.toDouble().seconds,
                    startDate = convertStringToLocalDataTime(it.startDate),
                    timeSpent = it.timeSpent.toDouble().seconds,
                    timeLeft = it.timeLeft.toDouble().seconds,
                    taskStatus = it.taskStatus,
                    taskLaunchTime = parseTaskLaunchTime(it.taskLaunchTime),
                    taskType = it.taskType
                )
            )
        }
        return appTaskList
    }

    private fun convertStringToLocalDataTime(string: String): LocalDateTime? {
        return if (string == "0.0" || string == "null") {
            null
        } else {
            LocalDateTime.ofEpochSecond(string.toLong(), 0, ZoneOffset.UTC)
        }
    }

    private fun convertingDProfileDataFromAppProfile(result: ProfileData): Profile {
        return DataProfile(
            name = result.name,
            id = result.idUser,
            email = result.email,
            avatarUrl = result.avatarUrl
        )
    }

    private fun parseTaskLaunchTime(taskLaunchTime: String?): LocalDateTime? {
        return if (taskLaunchTime.isNullOrEmpty() || taskLaunchTime == "null")
            null
        else
            LocalDateTime.parse(taskLaunchTime, formatter)
    }
}