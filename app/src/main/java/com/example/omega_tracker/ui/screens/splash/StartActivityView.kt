package com.example.omega_tracker.ui.screens.splash

import com.example.omega_tracker.ui.base_class.BaseView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END

interface StartActivityView : BaseView {
    @MoxyViewCommand(ADD_TO_END)
    fun gotoAuthorizationActivity()
    @MoxyViewCommand(ADD_TO_END)
    fun gotoMainActivity(token: String?)
}