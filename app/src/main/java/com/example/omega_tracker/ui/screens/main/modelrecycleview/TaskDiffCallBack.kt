package com.example.omega_tracker.ui.screens.main.modelrecycleview

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.omega_tracker.data.AppDataTask
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.entity.Task

class TaskDiffCallBack(
    private val oldTasks: MutableList<UiModel>, private val newTasks: MutableList<UiModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldTasks.size
    override fun getNewListSize(): Int = newTasks.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
         when {
            oldTasks[oldItemPosition] is UiModel.TaskModel && newTasks[newItemPosition] is UiModel.TaskModel -> {
                Log.d("MyLog", "TaskModel ${(oldTasks[oldItemPosition] as UiModel.TaskModel).task.id} ${(newTasks[newItemPosition] as UiModel.TaskModel).task.id}")
                return (oldTasks[oldItemPosition] as UiModel.TaskModel).task.id == (newTasks[newItemPosition] as UiModel.TaskModel).task.id
            }

            oldTasks[oldItemPosition] is UiModel.DividerModel && newTasks[newItemPosition] is UiModel.DividerModel -> {
                Log.d("MyLog","DividerModel ${(oldTasks[oldItemPosition] as UiModel.DividerModel).divider} ${(newTasks[newItemPosition] as UiModel.DividerModel).divider.currentState}")
                return (oldTasks[oldItemPosition] as UiModel.DividerModel).divider.currentState == (newTasks[newItemPosition] as UiModel.DividerModel).divider.currentState
            }

            oldTasks[oldItemPosition] is UiModel.RunningTaskModel && newTasks[newItemPosition] is UiModel.RunningTaskModel -> {
                Log.d("MyLog","RunningTaskModel ${(oldTasks[oldItemPosition] as UiModel.RunningTaskModel).runningTask.summary} ${(newTasks[newItemPosition] as UiModel.RunningTaskModel).runningTask.id}")
                return (oldTasks[oldItemPosition] as UiModel.RunningTaskModel).runningTask.id == (newTasks[newItemPosition] as UiModel.RunningTaskModel).runningTask.id
            }
            else -> return false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldTasks[oldItemPosition] is UiModel.TaskModel && newTasks[newItemPosition] is UiModel.TaskModel ->
                oldTasks[oldItemPosition] as UiModel.TaskModel == newTasks[newItemPosition] as UiModel.TaskModel

            oldTasks[oldItemPosition] is UiModel.DividerModel && newTasks[newItemPosition] is UiModel.DividerModel ->
                oldTasks[oldItemPosition] as UiModel.DividerModel == newTasks[newItemPosition] as UiModel.DividerModel

            oldTasks[oldItemPosition] is UiModel.RunningTaskModel && newTasks[newItemPosition] is UiModel.RunningTaskModel ->
                oldTasks[oldItemPosition] as UiModel.RunningTaskModel == newTasks[newItemPosition] as UiModel.RunningTaskModel
            else -> false
        }
    }
}