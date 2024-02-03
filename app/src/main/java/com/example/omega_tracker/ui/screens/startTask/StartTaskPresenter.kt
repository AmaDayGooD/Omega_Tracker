package com.example.omega_tracker.ui.screens.startTask


import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.R
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.StatisticsData
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.remote_data.dataclasses.*
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.service.ServiceTask
import com.example.omega_tracker.ui.base_class.BasePresenter
import com.example.omega_tracker.ui.screens.main.CustomTask
import com.example.omega_tracker.utils.FormatTime
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class StartTaskPresenter(
    private val id: String,
    private val token: String?,
    private val intent: Intent,
    private val settings: Settings
) : BasePresenter<StartTaskView>() {

    lateinit var profile: Profile

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        viewState.getFormatTime(formatTime)
        profile = settings.getProfile()
        launch {
            appRepository = AppRepository(coroutineContext, retrofit, dataBaseTasks)

            appRepository.getTaskById(id, token).collect { task ->
                Log.d("MyLog", "${task.taskType} ${task.summary} ")
                viewState.setVisibleButtonSettings(task.taskType)
                viewState.setTask(task)
            }
        }
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    @Inject
    lateinit var formatTime: FormatTime

    private lateinit var appRepository: AppRepository

    private var stateTask: List<StateBundleElement>? = arrayListOf()


    private var idTask: String = ""
    private var timeLeft: Duration = Duration.ZERO
    private var timeSpent: Duration = Duration.ZERO
    private var timeFromLaunch: Duration = Duration.ZERO
    private var title: String = ""

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val subscribeToTime = (service as ForegroundService.BinderFromService)
            subscribeToTime.subscribeToTime { time ->
                idTask = time.id
                if (intent.getStringExtra("id")!! != idTask) {
                    idTask = intent.getStringExtra("id")!!
                } else {
                    timeLeft = time.timeLeft
                    timeSpent = time.timeSpent
                    timeFromLaunch = time.timeFromLaunch
                    title = time.title
                    launch {
                        if (timeLeft != null && time.taskStatus == TaskStatus.Run) {
                            viewState.showLayoutStartAndComplete()
                        } else {
                            viewState.showLayoutStartButton()
                        }
                        timer(time)
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    fun returnConnection(): ServiceConnection {
        return connection
    }

    fun updateInfoTask() {
        launch {
            appRepository.getTaskById(id, token).collect {
                viewState.setTask(it)
            }
        }
    }

    fun updateStatus(id: String, newStatus: TaskStatus) {
        launch {
            appRepository.updateTaskStatus(id, newStatus)
        }
    }

    fun updateLaunchTime(id: String) {
        launch {
            appRepository.updateTimeLaunch(LocalDateTime.now(), id)
        }
    }

    fun postStateTask(result: DataForResult) {
        launch(Dispatchers.IO) {
            val successfully = appRepository.postStateTask(
                result.idTask!!, result.token!!, createBodyPostStateTask(result.idState!!)
            )
            Dispatchers.Main {
                if (successfully) {
                    viewState.showToast(
                        com.example.omega_tracker.Constants.TOAST_TYPE_SUCCESS,
                        R.string.status_changed_successfully
                    )
                } else viewState.showToast(
                    com.example.omega_tracker.Constants.TOAST_TYPE_ERROR,
                    R.string.status_changed_failed
                )

            }
        }
    }

    fun getTypeEnterTime(): Boolean {
        return settings.getTypeEnterTime()
    }

    fun getProfile() {

    }

    fun postTimeSpent(result: DataForResult, infoTask: Task) {
        launch {
            withContext(Dispatchers.IO) {
                val postRequest = appRepository.postTimeSpent(
                    result.idTask!!, result.token!!, createBodyPostTimeStent(profile.id, result)
                )
                withContext(Dispatchers.Main) {
                    if (postRequest) {

                        viewState.showToast(
                            com.example.omega_tracker.Constants.TOAST_TYPE_SUCCESS,
                            R.string.time_sent_successfully
                        )
                    } else viewState.showToast(
                        com.example.omega_tracker.Constants.TOAST_TYPE_ERROR,
                        R.string.error_try_later
                    )
                }
            }
        }
    }

    private fun createBodyPostTimeStent(
        idProfile: String, result: DataForResult
    ): TrackTimeBody {
        val duration = "${result.day}д${result.hour}ч${result.minute}м"
        return TrackTimeBody(
            duration = Duration(duration),
            text = result.comment!!,
            date = System.currentTimeMillis(),
            stateTask = result.idState!!,
            author = Author(idProfile)
        )
    }

    private fun createBodyCompletedTask(result: DataForResult, task: Task): StatisticsData {
        return StatisticsData(
            idTask = task.id,
            nameTask = task.summary,
            spentTime = "${result.day}д${result.hour}ч${result.minute}м",
            dataCompleted = LocalDateTime.now().toString()
        )
    }

    private fun createBodyPostStateTask(idState: String): StateTask {
        return StateTask(
            value = Id(idState)
        )
    }

    fun getAllNameProjects() {
        launch(Dispatchers.IO) {
            viewState.getAllNameProjects(appRepository.getAllNameProjects())
        }
    }

    fun getNameCustomField(string: String): String {
        return string.substringAfter("=").substringBefore(",")
    }

    fun timer(text: ServiceTask) {
        viewState.checkVisibleTextTimer(text)
    }

    fun setColorTextTimer(timeLeft: Duration) {
        if (timeLeft.inWholeSeconds < 0) viewState.setColorTextTimer(timeLeft)
    }

    fun getStateList() {
        launch(Dispatchers.IO) {
            stateTask = appRepository.getStateBundle(token!!)
            viewState.setState(stateTask)
        }
    }

    fun updateTimeCustomTask(result: DataForResult, infoTask: Task) {
        val timeSpent = (result.day.days + result.hour.hours + result.minute.minutes)
        launch(Dispatchers.IO) {
            appRepository.updateTaskStatus(result.idTask.toString(), TaskStatus.Open)
            appRepository.updateTimeCustomTask(timeSpent, result.idTask.toString())

            appRepository.insertCompletedTask(createBodyCompletedTask(result, infoTask))

            updateInfoTask()
        }
    }

    fun removeTaskLaunchTime(id: String) {
        launch {
            appRepository.removeTaskLaunchTime(id)
        }
    }

    fun currentStateTask(id: String) {
        launch {
            appRepository.getTaskById(id, token).collect {
                viewState.setCurrentTaskStatus(it.taskStatus)
            }
        }
    }

    fun removeTask(id: String) {
        launch {
            appRepository.removeTask(id)
            viewState.closeActivity()
        }
    }

    fun updateCustomTask(task: CustomTask) {
        launch {
            appRepository.updateCustomTask(task)
        }
    }
}


