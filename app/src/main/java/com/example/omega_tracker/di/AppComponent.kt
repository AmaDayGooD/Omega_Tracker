package com.example.omega_tracker.di

import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.ui.screens.Profile.ProfilePresenter
import com.example.omega_tracker.ui.screens.authorization.AuthorizationPresenter
import com.example.omega_tracker.ui.screens.main.MainPresenter
import com.example.omega_tracker.ui.screens.splash.StartPresenter
import com.example.omega_tracker.ui.screens.startTask.StartTaskPresenter
import com.example.omega_tracker.utils.FormatTime
import dagger.Component
import retrofit2.Retrofit

@Component(modules = [NetWorkModules::class, DataBase::class, ContextModule::class,UtilsModules::class])
interface AppComponent {
    fun inject(presenter: MainPresenter)
    fun inject(presenter: StartTaskPresenter)
    fun inject(presenter: StartPresenter)
    fun inject(presenter: AuthorizationPresenter)
    fun inject(presenter: ProfilePresenter)
    fun inject(appRepository: AppRepository)

    val data: TasksDao
    val retrofit: Retrofit
    val formatTime: FormatTime
}