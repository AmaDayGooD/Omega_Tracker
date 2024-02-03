package com.example.omega_tracker.ui.screens.statistics

import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.ui.base_class.BasePresenter

class StatisticsPresenter : BasePresenter<StatisticsView>() {

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
    }
}