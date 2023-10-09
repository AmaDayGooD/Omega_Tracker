package com.example.omega_tracker.modelrecycleview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.omega_tracker.*
import com.example.omega_tracker.data.repository.local_data.NameEntity
import com.example.omega_tracker.databinding.ItemTasksBinding
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity
import java.util.*
import kotlin.math.floor

class TasksAdapter() : ListAdapter<NameEntity, TasksAdapter.Holder>(Comparator())  {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemTasksBinding.bind(view)

        fun bind(tasks: NameEntity) = with(binding) {
            textNameTask.text = tasks.summary
            textNameProject.text = tasks.nameProject
            textCurrentState.text =tasks.currentState

            if (tasks.nameProject.length > 10 || textCurrentState.text.length > 10) {
                layoutNameProjectAndState.orientation = LinearLayout.VERTICAL
            } else
                layoutNameProjectAndState.orientation = LinearLayout.HORIZONTAL

            // Получение оставшегося времени в минутах на задачу
            val estimate = getMinutes(tasks.estimate) // Оценка времени
            val timeSpent = getMinutes(tasks.currentTime)// Затраченое время
            val timeLeft = estimate.toDouble() - timeSpent.toDouble() // Времени осталось

            // Перевод в дату
            val dataMills = tasks.startDate
            if (dataMills != null) {
                if (timeLeft < 0) {
                    textTimeTask.setTextColor(Color.parseColor("#FF0000"))
                }
                textTimeTask.text = formatMinutes(timeLeft.toString())
            } else textTimeTask.text = "Нет данных"

            // Обработка нажатия на кнопку запуска таймера
            buttonIconPlay.setOnClickListener {
                Toast.makeText(
                    itemView.context,"Запустил текущую задачу\n(не реализовано)",Toast.LENGTH_SHORT).show()
            }

            // обработка нажатия на item и передача данных на следующую активити
            layoutItemList.setOnClickListener {
                val infoTasks: String = tasks.id_tasks
                gotoNextActivity(itemView.context, infoTasks)
            }
        }

        private fun formatMinutes(minutes: String): String {
            val numbers: Double?
            return try {
                numbers = minutes.toDouble()
                val day = numbers / 1440
                val hours = (numbers / 60) % 24
                val remainingMinutes = numbers % 60
                Log.d("MyLog", "$numbers | $day | $hours | $remainingMinutes")
                when {
                    floor(day).toInt() > 0 -> "${floor(day).toInt()} д. ${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                    floor(hours) > 0 -> "${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                    else -> "${floor(remainingMinutes).toInt()} м."
                }
            } catch (e: NumberFormatException) {
                "0"
            }
        }

        private fun getMinutes(str: String): String {
            return if (str == "null") "0" else str.substringAfter("minutes=").substringBefore(", ")
        }

        private fun gotoNextActivity(context: Context, name: String) {
            val intent = Intent(context, StartTaskActivity::class.java)
            intent.putExtra("token", name)
            context.startActivity(intent)
        }
    }

    class Comparator : DiffUtil.ItemCallback<NameEntity>() {
        override fun areItemsTheSame(oldItem: NameEntity, newItem: NameEntity): Boolean {
            return oldItem.id_tasks == newItem.id_tasks
        }

        override fun areContentsTheSame(oldItem: NameEntity, newItem: NameEntity): Boolean {
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