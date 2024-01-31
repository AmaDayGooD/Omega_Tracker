package com.example.omega_tracker.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.omega_tracker.Constants.ESTIMATE
import com.example.omega_tracker.Constants.ID
import com.example.omega_tracker.Constants.TIME_LAUNCH
import com.example.omega_tracker.Constants.TIME_LEFT
import com.example.omega_tracker.Constants.TIME_SPENT
import com.example.omega_tracker.Constants.TITLE
import com.example.omega_tracker.utils.ParseTaskLaunchTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TaskManager(val context: Context) {

    private var mapRunningTask: MutableMap<String, RunningTask> = mutableMapOf()
    private val parseTaskLaunchTime = ParseTaskLaunchTime

    private var title: String = ""
    private var timeLeft: String = ""
    private var id: String = ""
    private var timeSpent: String = ""
    private var estimate: String = ""
    private var timeLaunch: LocalDateTime? = null

    private fun getDataFromIntent(intent: Intent?) {
        title = intent?.getStringExtra(TITLE).toString()
        timeLeft = intent?.getStringExtra(TIME_LEFT).toString()
        id = intent?.getStringExtra(ID).toString()
        timeSpent = intent?.getStringExtra(TIME_SPENT).toString()
        estimate = intent?.getStringExtra(ESTIMATE).toString()
        timeLaunch = parseTaskLaunchTime.parseTaskLaunchTime(intent?.getStringExtra(TIME_LAUNCH).toString())
    }



    fun startTask(intent: Intent?): Flow<ServiceTask> = flow {
        getDataFromIntent(intent)
        if (!mapRunningTask.containsKey(id)) {
            mapRunningTask[id] = RunningTask(intent)
            mapRunningTask[id]?.startTimer()?.collect { task ->
                emit(task)
            }
        } else {
            mapRunningTask[id] = RunningTask(intent)
        }
    }

    fun stopTimer(id: String?){
        mapRunningTask[id]?.stopTimer()
        mapRunningTask.remove(id)
    }

    fun toggleStatusTimer(id: String?) {
        mapRunningTask[id]?.toggleStatusTimer()
    }

    private fun log(text: String) {
        Log.d("MyLog", "$text")
    }

}