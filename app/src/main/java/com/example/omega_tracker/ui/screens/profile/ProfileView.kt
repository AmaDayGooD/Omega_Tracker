package com.example.omega_tracker.ui.screens.profile

import android.net.Uri
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.ui.base_class.BaseView
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StrategyType

interface ProfileView:BaseView {

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun loadImageProfile(uri: Uri)

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun setProfile(profile: Profile)
    @MoxyViewCommand(StrategyType.ADD_TO_END)
    fun setStateRadioButton(state:Boolean)
}