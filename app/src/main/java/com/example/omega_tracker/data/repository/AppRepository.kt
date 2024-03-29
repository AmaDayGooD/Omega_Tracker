package com.example.omega_tracker.data.repository

import com.example.omega_tracker.data.AppDataTask
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.*
import com.example.omega_tracker.data.remote_data.GetDataFromApi
import com.example.omega_tracker.data.remote_data.dataclasses.*
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.entity.StateTask
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.ui.screens.main.CustomTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class AppRepository @Inject constructor(
    override val coroutineContext: CoroutineContext,
    private val retrofit: Retrofit,
    private val dataBaseTasks: TasksDao

) : CoroutineScope {
    private val getDataFromApi = GetDataFromApi(retrofit)
    private val getDataFromBd = GetDataFromBd(retrofit, dataBaseTasks)

    suspend fun getAuthResult(token: String): Profile? {
        return getDataFromApi.getProfile(token)
    }

    suspend fun getTaskForRestore(): List<Task> {
        return getDataFromBd.getTaskForRestore()
    }

    suspend fun getAllTasks(token: String): Flow<MutableList<Task>?> = flow {
        val dataFromBD = getDataFromBd.getAllTaskBd()
        emit(dataFromBD)

        val dataFromApi = getDataFromApi.getAllTasksApi(token, dataBaseTasks, dataFromBD)
        if (dataFromApi != null) {
            val onlyCustomTask = dataFromBD.filter { it.taskType == TaskType.Custom }
            val taskFromApi = convertAppDataTaskFromTask(dataFromApi)

            taskFromApi.addAll(onlyCustomTask)
            emit(taskFromApi)
        }
    }

    private fun convertAppDataTaskFromTask(data: MutableList<AppDataTask>): MutableList<Task> {
        val list: MutableList<Task> = mutableListOf()
        list.addAll(data)
        return list
    }

    suspend fun getTaskById(idTask: String, token: String?): Flow<Task> = flow {
        val dataFromDb = getDataFromBd.getTaskById(idTask)
        emit(dataFromDb)
        val dataFromApi = getDataFromApi.getTaskById(idTask, token)
        if (dataFromApi != null) {
            emit(dataFromApi)
        }
    }

    suspend fun getTaskFromBdById(idTask: String): Task {
        return getDataFromBd.getTaskById(idTask)
    }

    suspend fun insertPendingTask(idTask: String, timeBodyTrackTime: BodyTrackTime) {
        val pendingTask = convertTrackTimeBodyToPendingData(idTask, timeBodyTrackTime)
        getDataFromBd.insertPendingTask(pendingTask)
    }


    suspend fun getStateBundle(token: String): List<StateTask> {
        val stateBundleApi:MutableList<StateTaskRemoteData>? = getDataFromApi.getStateBundle(token)
        stateBundleApi?.removeIf{it.localizedName == "Дубликат"}
        return if (stateBundleApi.isNullOrEmpty()) {
            getDataFromBd.getStateBundle()
        } else
            stateBundleApi
    }

    suspend fun setStateTask(stateTask: List<StateTask>) {
        getDataFromBd.setStateTask(stateTask)
    }

    suspend fun postTimeSpent(idTask: String, token: String, body: BodyTrackTime): Int {
        return getDataFromApi.postTimeSpent(token, body, idTask)
    }

    suspend fun insertCompletedTask(result: StatisticsLocalData) {
        getDataFromBd.insertCompletedTask(result)
    }

    suspend fun postStateTask(idTask: String, token: String, body: BodyStateTask): Boolean {
        return getDataFromApi.postStateTask(token, body, idTask)
    }

    suspend fun createCustomTask(customTask: CustomTask) {
        getDataFromBd.createCustomTask(convertCustomTaskForTaskData(customTask))
    }

    suspend fun getAllNameProjects(): List<String> {
        return getDataFromBd.getAllNameProjects()
    }

    suspend fun clearDataBase() {
        getDataFromBd.clearDataBase()
    }

    suspend fun updateTaskStatus(idTask: String, newStatus: TaskStatus) {
        getDataFromBd.updateTaskStatus(newStatus, idTask)
    }

    suspend fun updateTimeLaunch(timeNow: LocalDateTime, idTask: String) {
        dataBaseTasks.updateTimeLaunch(timeNow.toString(), idTask)
    }

    suspend fun getStatisticsToDay(toDay: LocalDateTime): List<Statistics> {
        val toMorrowStart = toDay.plusDays(1).toLocalDate()
        return getDataFromBd.getStatisticsToDay(toDay.toLocalDate(), toMorrowStart)
    }

    suspend fun getStatisticsToWeek(
        toDayStartWeek: LocalDateTime,
        toDayEndWeek: LocalDateTime
    ): List<Statistics> {
        return getDataFromBd.getStatisticsToWeek(toDayStartWeek, toDayEndWeek)
    }

    private fun convertPendingDataToTimeTrackBody(pendingTaskData: PendingTaskLocalData): BodyTrackTime {
        return BodyTrackTime(
            duration = Duration(pendingTaskData.duration),
            text = pendingTaskData.comment,
            date = pendingTaskData.date,
            stateTask = pendingTaskData.stateTask,
            author = Author(pendingTaskData.author)
        )
    }

    private fun convertTrackTimeBodyToPendingData(
        idTask: String,
        bodyTrackTime: BodyTrackTime
    ): PendingTaskLocalData {
        return PendingTaskLocalData(
            idTask = idTask,
            duration = bodyTrackTime.timeSpent,
            comment = bodyTrackTime.text,
            date = bodyTrackTime.date,
            stateTask = bodyTrackTime.stateTask,
            author = bodyTrackTime.authorId
        )
    }

    private fun convertCustomTaskForTaskData(customTask: CustomTask): TaskLocalData {
        return TaskLocalData(
            id_tasks = customTask.idTask!!,
            nameProject = customTask.nameProject!!,
            iconUrl = null,
            summary = customTask.summary!!,
            description = customTask.description!!,
            currentState = TaskStatus.Open.toString(),
            estimate = customTask.estimate!!.inWholeSeconds.toString(),
            startDate = (customTask.startDateTime / 100).toString(),
            timeSpent = "0",
            timeLeft = customTask.timeLeft!!.inWholeSeconds.toString(),
            taskStatus = TaskStatus.Open,
            taskType = TaskType.Custom,
            taskLaunchTime = null
        )
    }

    suspend fun updateTimeCustomTask(timeSpent: kotlin.time.Duration, idTask: String) {
        getDataFromBd.updateTimeCustomTask(timeSpent.inWholeSeconds.toString(), idTask)
    }

    suspend fun removeTaskLaunchTime(idTask: String) {
        getDataFromBd.removeTaskLaunchTime(idTask)
    }

    suspend fun removeTask(idTask: String) {
        getDataFromBd.removeTask(idTask)
    }

    suspend fun updateCustomTask(task: CustomTask) {
        getDataFromBd.updateCustomTask(convertCustomTaskToTaskData(task))
    }

    private suspend fun convertCustomTaskToTaskData(task: CustomTask): TaskLocalData {
        val oldTask = getDataFromBd.getTaskById(task.idTask!!)
        return TaskLocalData(
            id_tasks = oldTask.id,
            nameProject = task.nameProject.toString(),
            iconUrl = null,
            summary = task.summary.toString(),
            description = task.description.toString(),
            currentState = oldTask.currentState,
            estimate = task.estimate!!.inWholeSeconds.toString(),
            startDate = task.startDateTime.toString(),
            timeSpent = oldTask.timeSpent.inWholeSeconds.toInt().toString(),
            timeLeft = (task.estimate.inWholeMinutes * 60).toString(),
            taskStatus = oldTask.taskStatus,
            taskType = oldTask.taskType,
            taskLaunchTime = null
        )
    }
}