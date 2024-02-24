package com.example.omega_tracker.ui.screens.profile

import android.net.Uri
import android.util.Log
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.ui.base_class.BasePresenter
import com.example.omega_tracker.utils.FormatTime
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

class ProfilePresenter(private val settings: Settings) :
    BasePresenter<ProfileView>() {
    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        getProfile()
        viewState.setStateRadioButton(settings.getTypeEnterTime())
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    @Inject
    lateinit var formatTime: FormatTime

    private val appRepository = AppRepository(coroutineContext, retrofit, dataBaseTasks)

    private fun getProfile() {
        val profile = settings.getProfile()
        Log.d("MyLog", "profile $profile")
        viewState.loadImageProfile(Uri.parse("https://aleksandr152.youtrack.cloud${profile.avatar}"))
        viewState.setProfile(profile)
    }

    fun changeTypeEnterTime(value: Boolean) {
        settings.changeTypeEnterTime(value)
    }

    fun getTypeEnterTime(): Boolean {
        return settings.getTypeEnterTime()
    }

    fun deleteToken() {
        settings.deleteString()
        deleteProfile()
    }

    fun clearDataBase() {
        launch {
            appRepository.clearDataBase()
        }
    }

    private fun deleteProfile() {
        settings.deleteProfile()
    }
}