package com.example.omega_tracker.service

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.omega_tracker.Constants.CONTINUE
import com.example.omega_tracker.Constants.ESTIMATE
import com.example.omega_tracker.Constants.ID
import com.example.omega_tracker.Constants.PAUSE
import com.example.omega_tracker.Constants.RESTORE
import com.example.omega_tracker.Constants.START
import com.example.omega_tracker.Constants.STOP
import com.example.omega_tracker.Constants.TIME_LAUNCH
import com.example.omega_tracker.Constants.TIME_LEFT
import com.example.omega_tracker.Constants.TIME_SPENT
import com.example.omega_tracker.Constants.TITLE
import com.example.omega_tracker.R
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity.Companion.createPendingIntent
import com.example.omega_tracker.utils.FormatTime
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


private const val GROUP = "RUNNING_TASK"

class ForegroundService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var taskManager: TaskManager

    private var pauseTimer = false

    private var onProgress: ((ServiceTask) -> Unit)? = null
    private val formatTime = FormatTime
    private var list: MutableList<String> = mutableListOf()

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PAUSE -> pauseTimer(intent.getStringExtra(PAUSE))
                CONTINUE -> continueTimer(intent.getStringExtra(CONTINUE))
            }
        }
    }

    private val filter = IntentFilter().apply {
        addAction(PAUSE)
        addAction(CONTINUE)
    }

    companion object {
        fun startTimerService(context: Context, infoTask: Task): Intent {
            return Intent(context, ForegroundService::class.java).also {
                it.action = Action.START.toString()
                it.putExtra(TITLE, infoTask.summary)
                it.putExtra(TIME_LEFT, infoTask.remainingTime.inWholeMinutes)
                it.putExtra(ID, infoTask.id)
                it.putExtra(TIME_SPENT, infoTask.usedTime.inWholeMinutes)
                it.putExtra(ESTIMATE, infoTask.evaluate.inWholeMinutes)
                it.putExtra(TIME_LAUNCH, infoTask.taskLaunchTime.toString())
            }
        }

        fun stopTimerService(context: Context, idTask: String): Intent {
            Log.d("MyLog", "Service is Stop")
            return Intent(context, ForegroundService::class.java).also {
                it.action = Action.STOP.toString()
                it.putExtra(ID, idTask)
            }
        }

        fun completeTimerService(context: Context, idTask: String): Intent {
            return Intent(context, ForegroundService::class.java).also {
                it.action = Action.STOP.toString()
                it.putExtra(ID, idTask)
            }
        }
    }

    inner class BinderFromService : Binder() {
        fun subscribeToTime(onProgress: (ServiceTask) -> Unit) {
            this@ForegroundService.onProgress = onProgress
        }
    }

    override fun onCreate() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        taskManager = TaskManager(this)
        registerReceiver(broadCastReceiver, filter)
    }

    override fun onBind(intent: Intent?): IBinder {
        return BinderFromService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("MyLog", "action = ${intent?.action}")
            when (intent?.action) {
                START -> {
                    startTimer(taskManager, intent)
                }
                PAUSE -> {
                    pauseTimer(intent.getStringExtra(ID))
                }
                STOP -> {
                    stopTimer(intent.getStringExtra(ID))
                }
                CONTINUE -> {
                    continueTimer(intent.getStringExtra(ID))
                }
                RESTORE -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun startTimer(taskManager: TaskManager, intent: Intent?) {
        CoroutineScope(Dispatchers.Default).launch {
            taskManager.startTask(intent).collect { task ->
                if (task.taskStatus == TaskStatus.Open) {
                    list.remove(task.id)
                    NotificationManagerCompat.from(this@ForegroundService)
                        .cancel(task.id.hashCode())
                } else {
                    if (list.contains(task.id)) {
                        NotificationManagerCompat.from(this@ForegroundService).apply {
                            notify(
                                task.id.hashCode(), createNotification(
                                    task.title, task.timeSpent, task.id, applicationContext
                                )
                            )
                        }
                    } else {
                        list.add(task.id)
                        startForeground(
                            task.id.hashCode(), createNotification(
                                task.title, task.timeSpent, task.id, applicationContext
                            )
                        )
                    }
                }
                onProgress?.invoke(task)
            }
        }
    }

    private fun stopTimer(idTask: String?) {
        CoroutineScope(Dispatchers.Default).launch {
            taskManager.stopTimer(idTask)
            NotificationManagerCompat.from(this@ForegroundService).cancel(idTask.hashCode())
            stopSelf()
        }
    }

    private fun createNotification(
        title: String = "Загрузка...",
        timeSpentUpdate: Duration = 1.seconds,
        id: String,
        context: Context
    ): Notification {
        val intentPause = Intent(PAUSE).putExtra(PAUSE, id)
        val intentComplete = Intent(CONTINUE).putExtra(CONTINUE, id)

        return NotificationCompat.Builder(context, "running_channel")
            .setSmallIcon(R.drawable.ic_play_foreground).setContentTitle(title).setContentText(
                getString(R.string.time_past) + " " + formatTime.formatSeconds(
                    timeSpentUpdate
                )
            ).setSilent(true)
            .setContentIntent(createPendingIntent(this@ForegroundService, id))
            .setOngoing(true)
            .setPriority(2)
            .addAction(
                R.drawable.ic_pause_foreground, "PAUSE", PendingIntent.getBroadcast(
                    applicationContext, 12, intentPause, PendingIntent.FLAG_UPDATE_CURRENT
                )
            ).addAction(
                R.drawable.ic_pause_foreground, "Complete", PendingIntent.getBroadcast(
                    applicationContext, 0, intentComplete, PendingIntent.FLAG_UPDATE_CURRENT
                )
            ).setGroup(GROUP).build()
    }

    private fun groupNotification(): Notification {
        return NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_play_foreground).setColor(resources.getColor(R.color.main))
            .setSilent(true).setOngoing(true).setGroup(GROUP).setGroupSummary(true).build()
    }

    fun pauseTimer(stringExtra: String?) {
        taskManager.pauseTimer(stringExtra)
    }

    fun continueTimer(stringExtra: String?) {
        taskManager.continueTimer(stringExtra)
    }

    enum class Action {
        START, STOP, CONTINUE
    }

    override fun bindService(p0: Intent, conn: ServiceConnection, flags: Int): Boolean {
        return super.bindService(p0, conn, flags)
    }

    override fun onDestroy() {
        unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }
}