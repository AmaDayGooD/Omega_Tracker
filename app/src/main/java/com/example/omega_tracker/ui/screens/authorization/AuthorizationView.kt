package com.example.omega_tracker.ui.screens.authorization

import com.example.omega_tracker.data.repository.local_data.Settings
import com.example.omega_tracker.ui.base_class.BaseInterfase
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType

interface AuthorizationView : BaseInterfase {
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun setButtonColorError()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun setButtonColorRight()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun setButtonColorWarning()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun loadStart()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun loadEnd()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun setColorNormal()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun tokenIsEmpty()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun tokenIsNotEmpty()
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun checkInternet():Boolean
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun goMainScreen(token:String)
    @StateStrategyType(StrategyType.ADD_TO_END)
    fun initCacheToken(): Settings
}