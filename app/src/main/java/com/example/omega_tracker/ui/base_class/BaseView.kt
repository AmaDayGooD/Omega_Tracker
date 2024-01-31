package com.example.omega_tracker.ui.base_class

import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType

interface BaseView : OmegaView {

    @MoxyViewCommand(StrategyType.SKIP)
    fun showToast(toastType: Int, message: Int)

    @MoxyViewCommand(StrategyType.SKIP)
    fun log(message: String)
}