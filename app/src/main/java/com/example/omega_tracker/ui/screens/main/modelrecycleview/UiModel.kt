package com.example.omega_tracker.ui.screens.main.modelrecycleview

import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.entity.Task

sealed class UiModel{
    class RunningTaskModel(val runningTask: RunningTask):UiModel()
    class DividerModel(val divider:Divider):UiModel()
    class TaskModel(val task:Task):UiModel()
}
