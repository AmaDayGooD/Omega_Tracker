package com.example.omega_tracker.ui.screens.splash

import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.ui.base_class.BasePresenter
import retrofit2.Retrofit
import javax.inject.Inject
class StartPresenter(private val settings: Settings) : BasePresenter<StartActivityView>() {
    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        checkToken(getToken(settings))
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    private fun checkToken(token: String?) {
        if (token.isNullOrEmpty()) {
            viewState.gotoAuthorizationActivity()
        }
        else
            viewState.gotoMainActivity(token)
    }
}