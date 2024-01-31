package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.databinding.ItemRunningTaskBinding
import com.example.omega_tracker.ui.screens.startTask.StartTaskActivity

class RunningTaskHolder(private val itemView: View, private val listener: OnItemClickListener) :
    RecyclerView.ViewHolder(itemView) {

    private val binding = ItemRunningTaskBinding.bind(itemView)
    fun onBindView(task: UiModel.RunningTaskModel) = with(binding) {
        textCurrentTimeTask.text = task.runningTask.timeLeft
        textNameCurrentTask.text = task.runningTask.summary

        cardviewStartTask.setOnClickListener {
            itemView.context.startActivity(
                StartTaskActivity.createIntentStartTask(itemView.context, task.runningTask.id)
            )
        }
    }

}