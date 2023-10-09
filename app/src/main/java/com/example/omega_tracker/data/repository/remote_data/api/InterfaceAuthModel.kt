package com.example.omega_tracker.data.repository.remote_data.api

import android.util.Log
import androidx.room.RoomDatabase
import com.example.omega_tracker.data.repository.local_data.NameEntity
import com.example.omega_tracker.data.repository.local_data.Tasks_DAO
import com.example.omega_tracker.data.repository.remote_data.retrofit.dataclasses.AllData
import com.example.omega_tracker.data.repository.remote_data.retrofit.interfaces.TaskInterfase
import com.example.omega_tracker.data.repository.remote_data.retrofit.interfaces.UserInterfase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class InterfaceAuthModel @Inject constructor(
    override val coroutineContext: CoroutineContext,
    private var retrofit: Retrofit,
    private val dataBaseTasks: Tasks_DAO
) : CoroutineScope {

    suspend fun getAuthResult(token: String): Boolean {
        val user = retrofit.create(UserInterfase::class.java)
        return try {
            user.getUserOne(token).isSuccessful
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
    }

    suspend fun getAllTasks(token: String): MutableList<AllData> {
        val tasks = mutableListOf<AllData>()
        try {
            tasks.addAll(retrofit.create(TaskInterfase::class.java).getAllInfo(token))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tasks
    }

    suspend fun loadDataInDataBase(token: String?) {
        coroutineScope {
            launch {
                val result = getAllTasks(token.toString())

                result.forEach { value ->
                    dataBaseTasks.instertItem(filteringData(value))
                }
            }
        }
    }

    // Создание и заполнение data class для загрузки в БД
    private fun filteringData(result: AllData): NameEntity {
        return NameEntity(
            id_tasks = result.id,                                                           //id
            nameProject = result.project.name,                                              // Название проекта
            summary = result.summary,                                                       // Короткое описание
            description = result.description,                                               // Описание
            currentTime = getCurrentTime(result.customFields[9].value.toString()),          // Текущее прошедшее время
            currentState = getCurrentState(result.customFields[2].value.toString()),        // Текущее состояние задачи
            estimate = getCurrentTime(result.customFields[8].value.toString()),             // Оценка задачи
            startDate = convertStringToDouble(result.customFields[10].value.toString()),     // Дата начала задачи
            timeSpent = (getCurrentTime(result.customFields[9].value.toString()).toFloat()*60).toString(),
            timeLeft = (getCurrentTime(result.customFields[8].value.toString()).toFloat()*60
                    - (getCurrentTime(result.customFields[9].value.toString()).toFloat()*60)).toString()
        )
    }

    private fun convertStringToDouble(string: String): String {
        return if (string != "null") string.toDouble().toLong().toString() else "0.0"
    }

    // Получение значения времени сколько сейчас потрачено
    private fun getCurrentTime(str: String): String {
        return if (str == "null") "0" else str.substringAfter("minutes=").substringBefore(", ")
    }

    // Получение текущего состояния задачи
    private fun getCurrentState(str: String): String {
        return if (str == "null") "0" else str.substringAfter("name=").substringBefore(", ")
    }
}
