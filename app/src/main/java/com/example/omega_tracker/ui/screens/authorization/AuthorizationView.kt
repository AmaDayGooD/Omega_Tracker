package com.example.omega_tracker.ui.screens.authorization

import com.example.omega_tracker.ui.base_class.BaseView
import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.viewstate.strategy.*


interface AuthorizationView : BaseView {

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setButtonColorError()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setButtonColorRight()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setButtonColorWarning()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun loadStart()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun loadEnd()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setColorNormal()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun tokenIsEmpty()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun tokenIsNotEmpty()

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun checkInternet(token: String)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun goMainScreen(token: String)

    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun saveToken(token: String)

}