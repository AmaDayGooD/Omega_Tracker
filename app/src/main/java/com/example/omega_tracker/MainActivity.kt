package com.example.omega_tracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.omega_tracker.databinding.ActivityMainBinding
import com.example.omega_tracker.modelrecycleview.TasksAdapter
import com.example.omega_tracker.retrofit.dataclasses.AllData
import com.example.omega_tracker.retrofit.interfaces.TaskInterfase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var adapter = TasksAdapter()
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // проверка на наличие токена
        this.preferences = getSharedPreferences("token", MODE_PRIVATE)
        val defendToken = preferences.getString("token", "")
        if (defendToken.isNullOrEmpty()) {
            Toast.makeText(this, "Токен не введён", Toast.LENGTH_LONG).show()
            gotoAuth(this)
        }

        adapter = TasksAdapter()
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter


        val token = intent.getStringExtra("token")

        binding.curentTimeTask.text = "2:10:04"

        // Retrofit
        val retrofit = Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val tasks = retrofit.create(TaskInterfase::class.java)
        Log.d("MyLog", "передаваеный access token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            val allData = tasks.getAllInfo("$token")
            runOnUiThread {
                binding.apply {
                    val curentTime = Date(System.currentTimeMillis()) // Текущая дата
                    val outputFormat =
                        SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH) // Формат даты
                    val curentDate = outputFormat.format(curentTime) // Форматирование текущей даты
                    val curentDataItem: MutableList<AllData> = mutableListOf()
                    allData.forEach { allData ->
                        if (allData.customFields[10].value != null) {
                            val receivedDate = Date(allData.customFields[10].value.toString().toFloat().roundToLong())
                            val formatRreceivedDate = outputFormat.format(receivedDate)
                            if (curentDate == formatRreceivedDate) {
                                curentDataItem.add(allData)
                            }
                        }
                    }
                    adapter.submitList(curentDataItem)
                }
            }
        }
        binding.statistics.setOnClickListener {
            Toast.makeText(this, "Получай статистику", Toast.LENGTH_SHORT).show()
        }

        binding.time.setOnClickListener {
            Toast.makeText(this, "Получай время", Toast.LENGTH_SHORT).show()
            val s = preferences.getString("token", "")
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
        }

        binding.imgbutton.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.exitUser -> {
                        Toast.makeText(this, "Выходим...", Toast.LENGTH_SHORT).show()
                        val editor = preferences.edit()
                        editor.remove("token")
                        editor.apply()
                        gotoAuth(this)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_main)
            popupMenu.show()
        }

        binding.openAllTasks.setOnClickListener {
            Toast.makeText(this, "Открыл все", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gotoAuth(context: Context) {
        val intent = Intent(context, AuthorizationActivity::class.java)
        context.startActivity(intent)
    }
}

