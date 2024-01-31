package com.example.omega_tracker.ui.screens.Profile

import android.net.Uri
import android.util.Log
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.ui.base_class.BasePresenter
import com.example.omega_tracker.utils.FormatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

class ProfilePresenter(private val token: String, private val settings: Settings) :
    BasePresenter<ProfileView>() {
    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        getProfile()
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
}