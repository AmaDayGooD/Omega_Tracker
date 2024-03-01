package com.example.omega_tracker.ui.screens.startTask

import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.TaskType
import com.example.omega_tracker.data.remote_data.dataclasses.StateTaskRemoteData
import com.example.omega_tracker.entity.StateTask
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ServiceTask
import com.example.omega_tracker.ui.base_class.BaseView
import com.example.omega_tracker.utils.FormatTime
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType
import kotlin.time.Duration

interface StartTaskView : BaseView {
    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun getFormatTime(formatTime: FormatTime)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setProgressBarTimer(progressBar: CircularProgressBar, infoTask: Task)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setColorTextTimer(timeLeft: Duration)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun visibleButton()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun getAllNameProjects(allNameProjects:List<String>?)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setVisibleButtonSettings(taskType: TaskType)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun checkVisibleTextTimer(text: ServiceTask)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun showCurrentTime(currentTime: Duration)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun showTimeLeft(timeLeft: Duration)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun showLayoutStartButton()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun showLayoutStartAndComplete()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setState(state: List<StateTask>?)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun stopServices()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setTask(taskInfo: Task)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setCurrentTaskStatus(status: TaskStatus)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun closeActivity()

}