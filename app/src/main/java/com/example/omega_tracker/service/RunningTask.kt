package com.example.omega_tracker.service

import android.content.Intent
import android.util.Log
import com.example.omega_tracker.Constants.ESTIMATE
import com.example.omega_tracker.Constants.ID
import com.example.omega_tracker.Constants.TIME_LAUNCH
import com.example.omega_tracker.Constants.TIME_LEFT
import com.example.omega_tracker.Constants.TIME_SPENT
import com.example.omega_tracker.Constants.TITLE
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.utils.ParseTaskLaunchTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration as Duration
import java.time.Duration as Dura;

class RunningTask(intent: Intent?) {

    private val parseTaskLaunchTime = ParseTaskLaunchTime

    private var pauseTimer = false
    private var overTime = true

    private lateinit var title: String             // название
    private var timeLeft: Duration = ZERO     // оставшееся время
    private lateinit var id: String                // id
    private var timeSpent: Duration = ZERO    // прошло сейчас
    private var estimate: Duration = ZERO    // оценка
    private var timeLaunch: LocalDateTime? = null

    init {
        getDataFromIntent(intent)
    }

    private fun getDataFromIntent(intent: Intent?) {
        title = intent?.getStringExtra(TITLE).toString()
        timeLeft = intent?.getLongExtra(TIME_LEFT, 0)?.toInt()!!.minutes
        id = intent.getStringExtra(ID).toString()
        timeSpent = intent.getLongExtra(TIME_SPENT, 0).toInt().minutes
        estimate = intent.getLongExtra(ESTIMATE, 600).toInt().minutes
        timeLaunch = parseTaskLaunchTime.parseTaskLaunchTime(intent.getStringExtra(TIME_LAUNCH))
    }

    fun stopTimer() {
        overTime = false
    }

    fun toggleStatusTimer() {
        log("pauseTimer $pauseTimer $id")
        pauseTimer = !pauseTimer
    }

    fun pauseTimer() {
        log("pauseTimer")
        pauseTimer = true
    }

    fun continueTimer() {
        log("continueTimer")
        pauseTimer = false
    }


    fun startTimer(): Flow<ServiceTask> = flow {
        if (timeLaunch != null) {
            val difference = Dura.between(timeLaunch, LocalDateTime.now()).seconds

            timeSpent += difference.seconds
            timeLeft -= difference.seconds
            log("$difference || timeSpent $timeSpent || timeLeft $timeLeft")
        }
        var timeSpentUpdate = timeSpent
        var timeLeftUpdate = timeLeft
        var timeFromLaunch = ZERO

        pauseTimer = false
        while (overTime) {
            if (pauseTimer) {
                emit(
                    ServiceTask(
                        id = id,
                        title = title,
                        timeLeft = timeLeftUpdate,
                        timeSpent = timeSpentUpdate,
                        timeFromLaunch = timeFromLaunch,
                        taskStatus = TaskStatus.Pause
                    )
                )
                delay(1.seconds)
                continue
            }
            timeSpentUpdate += (1.seconds)
            timeLeftUpdate -= (1.seconds)
            timeFromLaunch = timeLeft.minus(timeLeftUpdate)
            emit(
                ServiceTask(
                    id = id,
                    title = title,
                    timeLeft = timeLeftUpdate,
                    timeSpent = timeSpentUpdate,
                    timeFromLaunch = timeFromLaunch,
                    taskStatus = TaskStatus.Run
                )
            )
            delay(1.seconds)
        }
        log("overTime $overTime")
        emit(
            ServiceTask(
                id = id,
                title = title,
                timeLeft = timeLeftUpdate,
                timeSpent = timeSpentUpdate,
                timeFromLaunch = timeFromLaunch,
                taskStatus = TaskStatus.Open
            )
        )
        overTime = true
    }


    private fun log(text: String) {
        Log.d("MyLog", text)
    }
}