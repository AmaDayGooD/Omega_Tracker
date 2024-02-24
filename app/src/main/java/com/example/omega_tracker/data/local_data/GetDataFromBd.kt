package com.example.omega_tracker.data.local_data

import android.util.Log
import androidx.core.net.toUri
import com.example.omega_tracker.data.AppDataTask
import com.example.omega_tracker.data.DataStatistics
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.entity.Task
import retrofit2.Retrofit
import java.time.LocalDate
import java.time.LocalDateTime
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

    suspend fun insertPendingTask(pendingTaskData: PendingTaskData) {
        dataBaseTasks.insertPendingTask(pendingTaskData)
    }

    suspend fun getPendingTask(): List<PendingTaskData> {
        return dataBaseTasks.getPendingTask()
    }

    suspend fun deletePendingTask(idTask: String) {
        dataBaseTasks.deletePendingTask(idTask)
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

    suspend fun insertCompletedTask(statisticsData: StatisticsData) {
        dataBaseTasks.insertCompletedTask(statisticsData)
    }

    suspend fun getStatisticsToDay(
        toDayStart: LocalDate,
        toMorrowStart: LocalDate
    ): List<Statistics> {
        return convertingStatisticsDataToStatistics(
            dataBaseTasks.getStatisticsToDay(
                toDayStart.toString(),
                toMorrowStart.toString()
            )
        )
    }

    suspend fun getStatisticsToWeek(
        toDayStartWeek: LocalDateTime,
        toDayEndWeek: LocalDateTime
    ): List<Statistics> {
        return convertingStatisticsDataToStatistics(
            dataBaseTasks.getStatisticsToWeek(
                toDayStartWeek.toString(),
                toDayEndWeek.toString()
            )
        )
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

    suspend fun clearDataBase() {
        dataBaseTasks.clearDataBase()
    }

    private fun convertingOneItemFromNameEntity(result: TaskData): AppDataTask {
        return AppDataTask(
            id = result.id_tasks,
            nameProject = result.nameProject,
            summary = result.summary,
            iconUrl = result.iconUrl?.toUri(),
            description = result.description,
            currentState = result.currentState,
            estimate = result.estimate.toDouble().seconds,
            startDate = LocalDateTime.ofEpochSecond(
                result.startDate.toLong(),
                0,
                ZoneOffset.UTC
            ),
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
                    iconUrl = it.iconUrl?.toUri(),
                    summary = it.summary,
                    description = it.description,
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

    private fun convertingStatisticsDataToStatistics(statisticsData: List<StatisticsData>): MutableList<Statistics> {
        val statistics = mutableListOf<Statistics>()
        statisticsData.forEach {
            statistics.add(
                DataStatistics(
                    idTask = it.idTask,
                    nameTask = it.nameTask,
                    spentTime = it.spentTime,
                    dataCompleted = it.dataCompleted
                )
            )
        }

        return statistics
    }

    private fun parseTaskLaunchTime(taskLaunchTime: String?): LocalDateTime? {
        return if (taskLaunchTime.isNullOrEmpty() || taskLaunchTime == "null")
            null
        else
            LocalDateTime.parse(taskLaunchTime, formatter)
    }
}