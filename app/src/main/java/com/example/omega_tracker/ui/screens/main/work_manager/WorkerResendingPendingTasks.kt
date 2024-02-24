package com.example.omega_tracker.ui.screens.main.work_manager


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.GetDataFromBd
import com.example.omega_tracker.data.local_data.PendingTaskData
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.remote_data.GetDataFromApi
import com.example.omega_tracker.data.remote_data.dataclasses.Author
import com.example.omega_tracker.data.remote_data.dataclasses.Duration
import com.example.omega_tracker.data.remote_data.dataclasses.TrackTimeBody
import com.example.omega_tracker.di.AppComponent
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
    private var bd: GetDataFromBd

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        val settings = Settings(applicationContext)
        token = settings.getToken().toString()
        api = GetDataFromApi(retrofit)
        bd = GetDataFromBd(retrofit, dataBaseTasks)
    }

    override suspend fun doWork(): Result {

        try {
            val listPendingTasks = bd.getPendingTask()
            listPendingTasks.forEach {task->
                Log.d("MyLog","${task.idTask} ${task.comment}")
                val convertingPendingTask = convertPendingDataToTimeTrackBody(task)
                api.postTimeSpent(token, convertingPendingTask, task.idTask)
                bd.updateTaskStatus(TaskStatus.Open,task.idTask)
                bd.deletePendingTask(task.idTask)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.d("MyLog", "${e.message}")
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun convertPendingDataToTimeTrackBody(pendingTaskData: PendingTaskData): TrackTimeBody {
        return TrackTimeBody(
            duration = Duration(pendingTaskData.duration),
            text = pendingTaskData.comment,
            date = pendingTaskData.date,
            stateTask = pendingTaskData.stateTask,
            author = Author(pendingTaskData.author)
        )
    }
}