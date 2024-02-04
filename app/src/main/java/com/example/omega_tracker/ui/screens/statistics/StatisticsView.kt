package com.example.omega_tracker.ui.screens.statistics

import com.example.omega_tracker.data.local_data.StatisticsData
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.ui.base_class.BaseView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType

interface StatisticsView:BaseView {
    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setCurrentStatistics(statistics: Map<String, Float>)
}