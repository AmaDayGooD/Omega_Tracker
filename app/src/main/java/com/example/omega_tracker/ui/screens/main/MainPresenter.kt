package com.example.omega_tracker.ui.screens.main

import android.content.ComponentName
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.DataProfile
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.ui.base_class.BasePresenter
import com.example.omega_tracker.ui.screens.main.modelrecycleview.DividerHolder
import com.example.omega_tracker.ui.screens.main.modelrecycleview.MultiViewAdapter
import com.example.omega_tracker.ui.screens.main.modelrecycleview.UiModel
import com.example.omega_tracker.utils.FormatTime
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.nanoseconds

class MainPresenter(
    private val token: String,
    private val settings: Settings,
    private val adapter: MultiViewAdapter
) :BasePresenter<MainView>() {

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao
    @Inject
    lateinit var formatTime: FormatTime

    private var profile: DataProfile? = null
    private var appRepository = AppRepository(coroutineContext, retrofit, dataBaseTasks)
    private var list: MutableList<Task> = mutableListOf()

    private val idRunningTask: MutableList<String> = mutableListOf()
    private val listRunningTask: MutableList<RunningTask> = mutableListOf()

    private var timeTask = ""
    private var idTask = ""
    private var titleTask = ""


    // Восстановление запущенных задач
    // после выгрузки приложения из памяти
    fun getTaskForRestore() {
        launch {
            loadCurrentDataTask(adapter, true)

            val result = appRepository.getTaskForRestore()
            result.forEach {
                viewState.restoreTask(it)
                val runningTask = RunningTask(
                    id = it.id,
                    summary = it.summary,
                    timeLeft = it.usedTime.inWholeSeconds.toString(),
                    taskStatus = it.taskStatus.toString()
                )
                idRunningTask.add(it.id)
                listRunningTask.add(runningTask)
                viewState.addItemRunningTask(runningTask)
                delay(5.nanoseconds)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val subscribeToTime = (service as ForegroundService.BinderFromService)
            subscribeToTime.subscribeToTime { task ->
                launch {
                    timeTask = formatTime.formatSeconds(task.timeSpent)
                    idTask = task.id
                    titleTask = task.title

                    if (timeTask.isEmpty()) {
                        return@launch
                    } else {
                        val runningTask = RunningTask(
                            id = idTask,
                            summary = titleTask,
                            timeLeft = timeTask,
                            taskStatus = task.taskStatus.toString()
                        )

                        if (!idRunningTask.contains(idTask)) {
                            idRunningTask.add(idTask)
                            listRunningTask.add(runningTask)
                            viewState.addItemRunningTask(runningTask)
                            updateStatus(idTask, TaskStatus.Run)
                            return@launch
                        }
                        val index = listRunningTask.indexOfFirst { it.id == idTask }
                        listRunningTask[index] = runningTask
                        viewState.updateListRunningTask(listRunningTask)
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    var listNotRunningTask = mutableListOf<UiModel>()

    fun getNotRunningTask() {
        launch {
            listRunningTask.forEach { task ->
                val answerTask = appRepository.getTaskFromBdById(task.id)

                if (answerTask.taskStatus != TaskStatus.Run) {
                    viewState.removeNotRunningTask(task)
                }
                listRunningTask.ifEmpty { return@launch }
            }
        }
    }

    fun returnConnection(): ServiceConnection {
        return connection
    }

    fun setProfile() {
        val profile = settings.getProfile()
        viewState.loadImageProfile(Uri.parse("https://aleksandr152.youtrack.cloud${profile.avatar}"))
    }

    private fun launchLoadCurrentDate() {
        viewState.restoreLoadBar()
        viewState.loadCurrentDataTasks()
    }

    // Получение всех задач из БД и отображение в recycleView
    fun launchLoadAllTasks() {
        viewState.restoreLoadBar()
        viewState.loadAllTasks()
    }

    fun getAllNameProjects() {
        launch(Dispatchers.IO) {
            viewState.getAllNameProjects(appRepository.getAllNameProjects())
        }
    }


    // Получение из БД задач на СЕГОДНЯ и отображение в recycleView
    fun loadCurrentDataTask(adapter: MultiViewAdapter, onFirst: Boolean): Boolean {
        var localOnFirst = onFirst
        launch {
            viewState.restoreLoadBar()
            withContext(Dispatchers.IO) {
                appRepository.getAllTasks(token).collect { result ->

                    val uiModelList: MutableList<UiModel> = mutableListOf()
                    withContext(Dispatchers.Main) {
                        list = checkCurrentDateItem(result)
                        list = removeRunningTask(list)

                        list.forEach {
                            uiModelList.add(UiModel.TaskModel(it))
                        }
                        if (localOnFirst) {
                            adapter.setData(uiModelList)
                            localOnFirst = false
                        } else {
                            adapter.updateListTasks(uiModelList, listNotRunningTask)
                        }

                        viewState.removeLoadBar()
                    }
                }
                getNotRunningTask()
                viewState.removeLoadBar()
            }
        }
        return false
    }

    // Проверка даты на текущую дату
    private fun checkCurrentDateItem(result: MutableList<Task>?): MutableList<Task> {
        val currentDataItem: MutableList<Task> = mutableListOf()
        val now = LocalDateTime.now().toLocalDate().atStartOfDay()
        result?.forEach { oneItem ->
            if (oneItem.onset != null && oneItem.onset!!.toLocalDate().atStartOfDay().isAfter(
                    now.minusDays(1)
                ) && oneItem.onset!!.toLocalDate().atStartOfDay()
                    .isBefore(now.plusDays(1).toLocalDate().atStartOfDay())
            ) {
                currentDataItem.add(oneItem)
            }
        }
        return currentDataItem
    }

    fun loadAllTasks(adapter: MultiViewAdapter, onFirst: Boolean = true): Boolean {
        var localOnFirst = onFirst
        launch {
            viewState.restoreLoadBar()
            withContext(Dispatchers.IO) {
                appRepository.getAllTasks(token).collect { result ->
                    val uiModelList: MutableList<UiModel> = mutableListOf()
                    withContext(Dispatchers.Main) {
                        list = removeRunningTask(result)

                        list.forEach {
                            uiModelList.add(UiModel.TaskModel(it))
                        }

                        if (localOnFirst) {
                            adapter.setData(uiModelList)
                            localOnFirst = false
                        } else {
                            adapter.updateListTasks(uiModelList)
                        }
                        viewState.removeLoadBar()
                    }
                }
                getNotRunningTask()
                viewState.removeLoadBar()
            }
        }
        return true
    }

    private fun removeRunningTask(list: MutableList<Task>?): MutableList<Task> {
        val result = mutableListOf<Task>()
        list?.forEach { item ->
            if (item.taskStatus == TaskStatus.Open) {
                result.add(item)
            }
        }
        return result
    }

    fun updateStatus(id: String, newStatus: TaskStatus) {
        launch {
            withContext(Dispatchers.IO) {
                appRepository.updateTaskStatus(id, newStatus)
                viewState.updateListTask()
            }
        }
    }

    fun createCustomTask(customTask: CustomTask) {
        launch {
            appRepository.createCustomTask(customTask)
        }
    }

    fun deleteToken() {
        settings.deleteString()
        deleteProfile()
    }

    fun clearDataBase() {
        launch {
            appRepository.clearDataBase()
        }
    }

    private fun deleteProfile() {
        settings.deleteProfile()
    }

    fun changeTypeEnterTime(value: Boolean) {
        settings.changeTypeEnterTime(value)
    }

    fun getInputTypeTime(): Boolean {
        return settings.getTypeEnterTime()
    }


    private fun log(message: String) {
        Log.d("MyLog", message)
    }
}