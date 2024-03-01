package com.example.omega_tracker.ui.screens.main.work_manager


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.GetDataFromBd
import com.example.omega_tracker.data.local_data.PendingTaskLocalData
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.remote_data.GetDataFromApi
import com.example.omega_tracker.data.remote_data.dataclasses.Author
import com.example.omega_tracker.data.remote_data.dataclasses.Duration
import com.example.omega_tracker.data.remote_data.dataclasses.BodyTrackTime
import retrofit2.Retrofit
import javax.inject.Inject

class WorkerResendingPendingTasks(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    private var token: String
    private var api: GetDataFromApi
    private var dataBase: GetDataFromBd

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        val settings = Settings(applicationContext)
        token = settings.getToken().toString()
        api = GetDataFromApi(retrofit)
        dataBase = GetDataFromBd(retrofit, dataBaseTasks)
        Log.d("MyLog", "Воркер добавился $this")
    }

    override suspend fun doWork(): Result {
        try {
            return if (checkInternetConnection()) {
                val listPendingTasks = dataBase.getPendingTask()
                listPendingTasks.forEach { task ->
                    Log.d("MyLog", "doWork ${task.idTask} ${task.comment}")
                    val convertingPendingTask = convertPendingDataToTimeTrackBody(task)
                    api.postTimeSpent(token, convertingPendingTask, task.idTask)
                    dataBase.updateTaskStatus(TaskStatus.Open, task.idTask)
                    dataBase.deletePendingTask(task.idTask)
                }
                Result.success()
            } else {
                Result.failure()
            }

        } catch (e: Exception) {
            Log.d("MyLog", "${e.message}")
            e.printStackTrace()
            return Result.failure()
        }
    }


    private fun checkInternetConnection(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
}