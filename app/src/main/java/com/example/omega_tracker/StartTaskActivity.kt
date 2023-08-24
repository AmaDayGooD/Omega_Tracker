package com.example.omega_tracker

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.omega_tracker.databinding.ActivityStartTaskBinding
import com.example.omega_tracker.retrofit.dataclasses.Tasks
import com.example.omega_tracker.retrofit.interfaces.TaskInterfase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Array.set
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class StartTaskActivity : AppCompatActivity() {
    lateinit var binding: ActivityStartTaskBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStartTaskBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val onlyStartButton = binding.onlyStartButton
        val pauseAndCompleteBtton = binding.startAndCompleteButton
        val startButton = binding.startbutton
        val pauseButton = binding.pauseButton
        val completeButton = binding.completeButton
        val backButton = binding.backButton
        val nameTask = binding.nameTask
        val descriptionTask = binding.descriptionTask

        val getMinutsInstance = getMinuts()
        var run = false


        val infoTask: Array<String> = intent.getStringArrayExtra("token")!!

        // проверка на количество слов
        val description = infoTask[1]
        val words = description.split(" ")
        val newDescription = if (words.size > 20) words.subList(0, 20)
            .joinToString(" ") + " ..." else description

        nameTask.text = infoTask[0]
        descriptionTask.text = newDescription

        // Retrofit
        val retrofit = Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val tasks = retrofit.create(TaskInterfase::class.java)

        this.preferences = getSharedPreferences("token", MODE_PRIVATE)
        var token = preferences.getString("token", "").toString()

        // Установка оценки времени в таймер
        var timeLeft = infoTask[5].toDouble()
        if (timeLeft < 0) {
            binding.apply {
                counttime.setTextColor(
                    ContextCompat.getColor(
                        this@StartTaskActivity,
                        R.color.error_color
                    )
                )
                counttime.text = getString(R.string.time_over, getMinutsInstance.formatMinutes(timeLeft.toString()))
                val typeFont = Typeface.create("rubik_extrabold", Typeface.BOLD)
                counttime.typeface = typeFont
            }
        } else
            Log.d("MyLog", "положительное ${getMinutsInstance.formatMinutes(infoTask[5])} ${infoTask[5]}")
        binding.counttime.text = getMinutsInstance.formatMinutes(infoTask[5])

        val circularProgressBar = binding.timer
        val timer = Timer()
        val estimate = infoTask[3].toFloat() // оценка 1ч10м
        val timeSpent = infoTask[4].toFloat() // время потрачено 15м
        var curentTime: Float = timeSpent*60

        circularProgressBar.progressMax = estimate*60
        circularProgressBar.progress = timeSpent*60
        Log.d("MyLog","estimate ${estimate * 60} ${timeSpent*60}")
        var sec = 0  // пройденные секунды

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Изменение подписи к таймеру
                if (sec % 60 == 0) {
                    Log.d("MyLog", "СМЕНА!!!")
                    timeLeft -= 1
                }
                sec++
                curentTime++
                Log.d("MyLog","curentTime $curentTime | timeLeft $timeLeft | sec $sec")
                runOnUiThread {
                    //Log.d("MyLog","estimate $estimate | temp $temp | ${estimate + temp}" )
                    binding.counttime.text = getMinutsInstance.formatMinutes(timeLeft.toString())

                    if(curentTime==estimate*60) {
                        Toast.makeText(this@StartTaskActivity, "Таймер остановлен", Toast.LENGTH_LONG).show()
                        circularProgressBar.progressBarColorStart = Color.BLUE
                        circularProgressBar.backgroundProgressBarColorStart = Color.GRAY
                        circularProgressBar.backgroundProgressBarColorEnd = Color.RED
                        circularProgressBar.progressBarWidth = 10f
                        var progress = 10f
                        circularProgressBar.progress = progress

                        timer.scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                progress++
                                circularProgressBar.progress = progress
                            }

                        },1000,1000)

                        binding.counttime.text = "Время вышло"
                        cancel()
                    }
                }
                circularProgressBar.progress = curentTime
            }
        }, 1000, 1000)


        // Обработка нажатия на кнопки
        startButton.setOnClickListener {
            run = true
            onlyStartButton.visibility = View.GONE
            pauseAndCompleteBtton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            run = false
            Toast.makeText(this, "Пауза", Toast.LENGTH_LONG).show()
        }

        completeButton.setOnClickListener {
            Toast.makeText(this, "Complete task", Toast.LENGTH_LONG).show()
            onlyStartButton.visibility = View.VISIBLE
            pauseAndCompleteBtton.visibility = View.GONE
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}