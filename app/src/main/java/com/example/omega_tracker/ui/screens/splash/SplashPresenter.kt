package com.example.omega_tracker.ui.screens.splash

import android.content.Context
import android.content.Intent
import com.example.omega_tracker.data.repository.local_data.Settings
import com.example.omega_tracker.data.repository.local_data.TasksDB
import com.example.omega_tracker.data.repository.local_data.Tasks_DAO
import com.example.omega_tracker.ui.base_class.BasePresenter
import com.example.omega_tracker.ui.screens.authorization.AuthorizationActivity
import com.example.omega_tracker.ui.screens.main.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


class SplashPresenter @Inject constructor(private val view: SplashActivity) : BasePresenter() {

    fun checkToken(token: String?){
        if(token.isNullOrEmpty()){
            gotoAuthorizationActivity()
        }
        else
            gotoMainActivity(token)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object RetrofitInstance {
        @Provides
        @Singleton
        fun getRetrofitOne() : Retrofit{
            return Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object DataBaseInstance{
        @Provides
        @Singleton
        fun getDataBaseOne(@ApplicationContext context: Context): Tasks_DAO {
            return TasksDB.createDataBase(context).dao
        }
    }

    private fun gotoMainActivity(token:String?){
        val intent = Intent(view, MainActivity::class.java)
        intent.putExtra("token", token)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        view.startActivity(intent)
    }

    private fun gotoAuthorizationActivity() {
        val intent = Intent(view, AuthorizationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        view.startActivity(intent)
    }

    fun getToken(authCacheToken: Settings) : String? {
        return authCacheToken.getString()
    }
}