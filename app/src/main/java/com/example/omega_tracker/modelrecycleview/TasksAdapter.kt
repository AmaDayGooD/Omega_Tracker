package com.example.omega_tracker.modelrecycleview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.omega_tracker.*
import com.example.omega_tracker.databinding.ItemTasksBinding
import com.example.omega_tracker.retrofit.dataclasses.AllData
import com.example.omega_tracker.retrofit.dataclasses.Tasks
import okhttp3.internal.toHexString
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt


class TasksAdapter : ListAdapter<AllData, TasksAdapter.Holder>(Comparator()) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemTasksBinding.bind(view)
        fun bind(tasks: AllData) = with(binding) {
            descriptionTask.text = tasks.description
            nameTask.text = tasks.summary


            // Получение оставшегося времени в минутах на задачу
            val estimate = getMinutes(tasks.customFields[8].value.toString()) // Оценка времени
            val timeSpent = getMinutes(tasks.customFields[9].value.toString())// Затраченое время
            val timeLeft = estimate.toDouble() - timeSpent.toDouble() // Времени осталось
            Log.d("MyLog", "timeLeft ${timeLeft.toString()}")

            // Перевод в дату
            val dataMills = tasks.customFields[10].value
            if (dataMills != null) {
                if (timeLeft < 0) {
                    timeTask.setTextColor(Color.parseColor("#FF0000"))
                }
                timeTask.text = formatMinutes(timeLeft.toString())
                Log.d("MyLog","бла бла $timeLeft ${timeTask.text}")
            } else timeTask.text = "Нет данных"

            // Обработка нажатия на кнопку запуска таймера
            itemiconPlay.setOnClickListener {
                Toast.makeText(
                    itemView.context,
                    "Запустил текущую задачу\n(не реализовано)",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // обработка нажатия на item и передача данных на следующую активити
            itemList.setOnClickListener {
                val infoTasks: Array<String> =
                    arrayOf(
                        tasks.summary,
                        tasks.description,
                        tasks.id,
                        estimate,
                        timeSpent,
                        timeLeft.toString()
                    )
                Log.d("MyLog","new timeLeft $timeLeft ")
                gotoNextActivity(itemView.context, infoTasks)
            }
        }

        private fun formatMinutes(minutes: String): String {
            val numbers: Double?
            return try {
                numbers = minutes.toDouble()
                Log.d("MyLog", numbers.toString())
                val day = numbers / 1440

                val hours = (numbers / 60) % 24
                val remainingMinutes = numbers % 60
                Log.d("MyLog", "$numbers | $day | $hours | $remainingMinutes")
                when {
                    floor(day).toInt()>0 -> "${floor(day).toInt()} д. ${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                    floor(hours) > 0 -> "${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                    else -> "${floor(remainingMinutes).toInt()} м."
                }
            } catch (e: NumberFormatException) {
                Log.d("MyLog", e.message.toString())
                "0"
            }
        }

        private fun getMinutes(str: String): String {
            return if (str == "null") "0" else str.substringAfter("minutes=").substringBefore(", ")
        }

        private fun gotoNextActivity(context: Context, name: Array<String>) {
            val intent = Intent(context, StartTaskActivity::class.java)
            intent.putExtra("token", name)
            context.startActivity(intent)
        }
    }

    class Comparator : DiffUtil.ItemCallback<AllData>() {
        override fun areItemsTheSame(oldItem: AllData, newItem: AllData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AllData, newItem: AllData): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_tasks, parent, false)
        return Holder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        if (item != null)
            holder.bind(item)
    }
}