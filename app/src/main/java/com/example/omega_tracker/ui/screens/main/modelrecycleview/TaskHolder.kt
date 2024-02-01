package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.omega_tracker.databinding.ItemTasksBinding
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity
import com.example.omega_tracker.utils.FormatTime

class TaskHolder(private val itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemTasksBinding.bind(itemView)
    private val formatTime = FormatTime
    fun onBindView(itemTask: Task) = with(binding) {
        textNameTask.text = itemTask.summary
        textNameProject.text = itemTask.nameProject
        textCurrentState.text = itemTask.currentState
        textTimeTask.text = formatTime.formatSeconds(itemTask.remainingTime)

        if (itemTask.nameProject.length > 10 || textCurrentState.text.length > 10) {
            layoutNameProjectAndState.orientation = LinearLayout.VERTICAL
        } else layoutNameProjectAndState.orientation = LinearLayout.HORIZONTAL

        // Получение оставшегося времени в минутах на задачу
        val timeLeft = itemTask.remainingTime // Времени осталось

        // Перевод в дату
        val dataMills = itemTask.onset
        if (dataMills != null) {
            if (timeLeft.isNegative()) {
                textTimeTask.setTextColor(Color.RED)
            } else textTimeTask.setTextColor(Color.GRAY)
            textTimeTask.text = formatTime.formatSeconds(timeLeft)
        } else textTimeTask.text = "Нет данных"


        // Обработка нажатия на кнопку запуска таймера
        buttonIconPlay.setOnClickListener {
            itemView.context.startForegroundService(
                ForegroundService.startTimerService(
                    itemView.context, itemTask
                )
            )

            Toast.makeText(
                itemView.context, "Запустил задачу ${itemTask.summary}", Toast.LENGTH_SHORT
            ).show()
        }

        // обработка нажатия на item и передача данных на следующую активити
        layoutItemList.setOnClickListener {
            itemView.context.startActivity(
                StartTaskActivity.createIntentStartTask(
                    itemView.context, itemTask.id
                )
            )
        }
    }
}