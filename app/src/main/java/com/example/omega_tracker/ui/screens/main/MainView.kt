package com.example.omega_tracker.ui.screens.main

import android.net.Uri
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.ui.base_class.BaseView
import com.example.omega_tracker.ui.screens.main.modelrecycleview.UiModel
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType

interface MainView : BaseView {

    @MoxyViewCommand(StrategyType.SINGLE)
    fun restoreTask(tasks: Task)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun addItemRunningTask(itemLRunningTask: RunningTask)

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun removeNotRunningTask(notRunningTask: RunningTask)

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun updateRunningTask(listRunningTask: RunningTask)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun loadCurrentDataTasks()

    @MoxyViewCommand(StrategyType.SINGLE)
    fun loadAllTasks()

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun loadImageProfile(uri: Uri)

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun getAllNameProjects(allNameProjects: List<String>?)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun gotoAuth()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun removeLoadBar()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun restoreLoadBar()

//    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
//    fun updateListTask()
}