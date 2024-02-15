package com.example.omega_tracker.ui.base_class


import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.repository.AppRepository
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.views.OmegaView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import javax.inject.Inject

open class BasePresenter<View:OmegaView>: OmegaPresenter<View>(),CoroutineScope {
    override fun onDestroy() {
        job.cancel()
    }
    fun getToken(settings: Settings): String? {
        return settings.getToken()
    }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main.immediate + job
}