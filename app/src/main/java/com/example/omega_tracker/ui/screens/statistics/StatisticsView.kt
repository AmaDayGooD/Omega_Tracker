package com.example.omega_tracker.ui.screens.statistics

import com.example.omega_tracker.data.local_data.StatisticsData
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.ui.base_class.BaseView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType
import java.time.LocalDateTime

interface StatisticsView : BaseView {
    @MoxyViewCommand(StrategyType.SINGLE)
    fun setCurrentStatistics(statistics: Map<String, Float>)
    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setTimeSpent(value: Float)

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun setNumberOfCompletedTasks(number:Int)
    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun addDayInListShowDays(day: LocalDateTime)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun addNextDay(nextDay:Map<String, Float>)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun addPreviewDay(nextDay:Map<String, Float>)
}