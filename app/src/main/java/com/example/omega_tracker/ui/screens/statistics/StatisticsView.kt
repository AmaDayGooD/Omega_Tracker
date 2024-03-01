package com.example.omega_tracker.ui.screens.statistics

import com.example.omega_tracker.ui.base_class.BaseView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
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
    fun addDateInListDate(day: LocalDateTime)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun addNextDayOrWeek(nextDay:Map<String, Float>)

    @MoxyViewCommand(StrategyType.SINGLE)
    fun addPreviewDayOrWeek(nextDay:Map<String, Float>)
}