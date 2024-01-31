package com.example.omega_tracker.data.remote_data

import android.util.Log
import com.example.omega_tracker.data.DataProfile
import com.example.omega_tracker.data.local_data.TaskData
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.remote_data.interfaces.YouTrackApi
import com.example.omega_tracker.data.AppDataTask
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.ProfileData
import com.example.omega_tracker.data.local_data.TaskType
import com.example.omega_tracker.data.remote_data.dataclasses.*
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.entity.Task
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class GetDataFromApi @Inject constructor(
    private val retrofit: Retrofit
) {
    private val apiInterface = retrofit.create(YouTrackApi::class.java)

    suspend fun getAllTasksApi(
        token: String, dataBaseTasks: TasksDao, dataFromDb: MutableList<Task>
    ): MutableList<AppDataTask>? {
        val tasks = mutableListOf<AllData>()
        try {
            tasks.addAll(apiInterface.getAllInfo(token))

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (dataFromDb.isEmpty()) {
            convertingDataListFromAllData(tasks).forEach { task ->
                dataBaseTasks.insertTask(convertingDataFromAppTask(task))
            }
            convertingDataListFromAllData(tasks)
        } else {
            convertingDataListFromAllData(tasks, dataFromDb).forEach { task ->
                dataFromDb.forEach { dataFromBd ->
                    if (dataFromBd.id == task.id) {
                        dataBaseTasks.insertTask(convertingDataFromAppTask(task, dataFromBd))
                    }
                }
            }
            convertingDataListFromAllData(tasks, dataFromDb)
        }
    }

    suspend fun getTaskById(id: String, token: String?): AppDataTask? {
        val oneTask: AppDataTask
        try {
            oneTask = convertingDataListFromAllData(apiInterface.getUserById(id, token))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return oneTask
    }

    suspend fun getProfile(token: String?): Profile? {
        val profile: UserBody
        try {
            profile = apiInterface.getUserOne(token)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return convertingUserBodyFromAppDataProfile(profile)
    }

    suspend fun getStateBundle(token: String): List<StateBundleElement>? {
        val list: MutableList<StateBundleElement> = mutableListOf()
        try {
            list.addAll(apiInterface.getStateBundleElement(token))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return list
    }

    suspend fun postTimeSpent(token: String?, body: TrackTimeBody, idTask: String): Boolean {
        try {
            apiInterface.postTimeSpent(token, body, idTask)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return false
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return false
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    suspend fun postStateTask(token: String?, body: StateTask, idTask: String): Boolean {
        try {
            apiInterface.postStateTask(token, body, idTask)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return false
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return false
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun convertingDataListFromAllData(
        result: MutableList<AllData>,
        dataFromDb: MutableList<Task>
    ): MutableList<AppDataTask> {
        val appTaskList = mutableListOf<AppDataTask>()

        result.forEach{  data ->
            appTaskList.add(
                AppDataTask(
                    id = data.id,                                                                 //id
                    nameProject = data.project.name,                                              // Название проекта
                    summary = data.summary,                                                       // Короткое описание
                    description = data.description,                                               // Описание
                    currentTime = getCurrentTime(data.customFields[9].value.toString()),          // Текущее прошедшее время
                    currentState = getCurrentState(data.customFields[2].value.toString()),        // Текущее состояние задачи
                    estimate = getCurrentTime(data.customFields[8].value.toString()),             // Оценка задачи
                    startDate = convertStringToLocalDataTime(data.customFields[10].value.toString()),     // Дата начала задачи
                    timeSpent = (getCurrentTime(data.customFields[9].value.toString())),
                    timeLeft = ((getCurrentTime(data.customFields[8].value.toString()))
                            - (getCurrentTime(data.customFields[9].value.toString()))),
                    taskStatus = dataFromDb.find { it.id == data.id }?.taskStatus!!,
                    taskLaunchTime = dataFromDb.find { it.id == data.id }?.taskLaunchTime,
                    taskType = TaskType.YouTrack
                )
            )
        }
        return appTaskList
    }

    private fun convertingDataListFromAllData(
        result: MutableList<AllData>
    ): MutableList<AppDataTask> {
        val appTaskList = mutableListOf<AppDataTask>()
        result.forEach {
            appTaskList.add(
                AppDataTask(
                    id = it.id,                                                                 //id
                    nameProject = it.project.name,                                              // Название проекта
                    summary = it.summary,                                                       // Короткое описание
                    description = it.description,                                               // Описание
                    currentTime = getCurrentTime(it.customFields[9].value.toString()),          // Текущее прошедшее время
                    currentState = getCurrentState(it.customFields[2].value.toString()),        // Текущее состояние задачи
                    estimate = getCurrentTime(it.customFields[8].value.toString()),             // Оценка задачи
                    startDate = convertStringToLocalDataTime(it.customFields[10].value.toString()),     // Дата начала задачи
                    timeSpent = (getCurrentTime(it.customFields[9].value.toString())),
                    timeLeft = (getCurrentTime(it.customFields[8].value.toString())
                            - (getCurrentTime(it.customFields[9].value.toString()))),
                    taskStatus = TaskStatus.Open,
                    taskLaunchTime = null,
                    taskType = TaskType.YouTrack
                )
            )
        }
        return appTaskList
    }

    private fun convertingDataListFromAllData(result: AllData): AppDataTask {
        return AppDataTask(
            id = result.id,                                                                 //id
            nameProject = result.project.name,                                              // Название проекта
            summary = result.summary,                                                       // Короткое описание
            description = result.description,                                               // Описание
            currentTime = getCurrentTime(result.customFields[9].value.toString()),          // Текущее прошедшее время
            currentState = getCurrentState(result.customFields[2].value.toString()),        // Текущее состояние задачи
            estimate = getCurrentTime(result.customFields[8].value.toString()),             // Оценка задачи
            startDate = convertStringToLocalDataTime(result.customFields[10].value.toString()),     // Дата начала задачи
            timeSpent = (getCurrentTime(result.customFields[9].value.toString())),
            timeLeft = ((getCurrentTime(result.customFields[8].value.toString()))
                    - (getCurrentTime(result.customFields[9].value.toString()))),
            taskStatus = TaskStatus.Open,
            taskLaunchTime = null,
            taskType = TaskType.YouTrack
        )
    }

    private fun convertingDataFromAppTask(result: AppDataTask): TaskData {
        return TaskData(
            id_tasks = result.id,
            nameProject = result.nameProject,
            summary = result.summary,
            description = result.description,
            currentTime = result.currentTime.inWholeSeconds.toString(),
            currentState = result.currentState,
            estimate = result.estimate.inWholeSeconds.toString(),
            startDate = (result.startDate?.toInstant(ZoneOffset.UTC)?.toEpochMilli())?.div(1000).toString(),
            timeSpent = result.timeSpent.inWholeSeconds.toString(),
            timeLeft = result.timeLeft.inWholeSeconds.toString(),
            taskStatus = TaskStatus.Open,
            taskType = TaskType.YouTrack,
            taskLaunchTime = null
        )
    }

    private fun convertingDataFromAppTask(result: AppDataTask, dataFromDb: Task): TaskData {
        return TaskData(
            id_tasks = result.id,
            nameProject = result.nameProject,
            summary = result.summary,
            description = result.description,
            currentTime = result.currentTime.inWholeSeconds.toString(),
            currentState = result.currentState,
            estimate = result.estimate.inWholeSeconds.toString(),
            startDate = (result.startDate?.toInstant(ZoneOffset.UTC)?.toEpochMilli())?.div(1000).toString(),
            timeSpent = result.timeSpent.inWholeSeconds.toString(),
            timeLeft = result.timeLeft.inWholeSeconds.toString(),
            taskStatus = dataFromDb.taskStatus,
            taskType = TaskType.YouTrack,
            taskLaunchTime = dataFromDb.taskLaunchTime.toString()
        )
    }

    private fun convertingUserBodyFromAppDataProfile(result: UserBody): Profile {
        return DataProfile(
            name = result.name,
            id = result.id,
            email = result.email,
            avatarUrl = result.avatarUrl
        )
    }

    // Получение значения времени сколько сейчас потрачено
    private fun getCurrentTime(str: String): kotlin.time.Duration {
        return (if (str == "null") "0" else str.substringAfter("minutes=").substringBefore(", ")).toDouble().minutes
    }

    // Получение текущего состояния задачи
    private fun getCurrentState(str: String): String {
        return if (str == "null") "0" else str.substringAfter("name=").substringBefore(", ")
    }

    // Перевод строки в тип Double
    private fun convertStringToLocalDataTime(string: String): LocalDateTime? {
        return if (string != "null") LocalDateTime.ofEpochSecond(string.toDouble().toLong()/1000,0, ZoneOffset.UTC) else null
    }

}