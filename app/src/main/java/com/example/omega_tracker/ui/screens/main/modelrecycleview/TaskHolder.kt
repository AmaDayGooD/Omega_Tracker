package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.omega_tracker.R
import com.example.omega_tracker.databinding.ItemTasksBinding
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity
import com.example.omega_tracker.utils.FormatTime
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import com.omega_r.libs.omegatypes.image.setImage

class TaskHolder(private val itemView: View, private val listener: OnItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
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

        if (itemTask.iconUrl == null) {
            imageviewIconProject.setImageResource(R.mipmap.ic_launcher)
        } else {
            val uri = Uri.parse("https://aleksandr152.youtrack.cloud${itemTask.iconUrl}")
            listener.getGlideToVector()
                .withListener(object : GlideToVectorYouListener {
                    override fun onLoadFailed() {}

                    override fun onResourceReady() {}
                }).load(uri, imageviewIconProject)
        }

        // Обработка нажатия на кнопку запуска таймера
        buttonIconPlay.setOnClickListener {
            listener.onClickRunningTask(itemTask)
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