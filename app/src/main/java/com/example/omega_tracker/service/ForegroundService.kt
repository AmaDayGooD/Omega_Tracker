package com.example.omega_tracker.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.util.Executors
import com.example.omega_tracker.R
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity.Companion.createPendingIntent
import com.example.omega_tracker.ui.screens.startTask.StartTaskPresenter

class ForegroundService : Service(){

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MyLog", "OUTS: onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title")
        val text = intent?.getStringExtra("timeLeft")
        val id = intent?.getStringExtra("id")
        when(intent?.action){
            Action.START.toString() -> start(title!!,text!!, id!!)
            Action.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(title:String, text:String, id:String){
        Log.d("MyLog", "OUTS: start")

        val stopTimer = NotificationCompat.Action.Builder(
            R.drawable.ic_pause_foreground,
            "Остановить таймер",
            createPendingIntent(this, id)
        ).build()

        val finishTask = NotificationCompat.Action.Builder(
            R.drawable.ic_complete_foreground,
            "Завершить задачу",
            createPendingIntent(this,id)
        ).build()

        val notification = NotificationCompat.Builder(this,  "running_channel")
            .setSmallIcon(R.drawable.ic_play_foreground)
            .setContentTitle(title)
            .setContentText("Времени осталось $text")
            .setSilent(true)
            .setContentIntent(createPendingIntent(this, id))
            .addAction(stopTimer)
            .addAction(finishTask)
            .build()
        startForeground(10,notification)
    }

    enum class Action{
        START, STOP
    }
}
