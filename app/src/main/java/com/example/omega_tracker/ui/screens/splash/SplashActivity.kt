package com.example.omega_tracker.ui.screens.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.omega_tracker.R
import com.example.omega_tracker.data.repository.local_data.Settings
import com.example.omega_tracker.databinding.ActivitySplashBinding
import com.example.omega_tracker.ui.screens.authorization.AuthorizationActivity
import com.example.omega_tracker.ui.screens.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.function.LongFunction
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), SplashActivityView {
    lateinit var binding: ActivitySplashBinding

    private lateinit var presenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter = SplashPresenter(this)
        presenter.checkToken(initCacheToken())

    }

    private fun initCacheToken():String?{
        val authCacheToken = Settings(this)
        return presenter.getToken(authCacheToken)
    }

    override fun showToast(toastType: Int, message: Int) {
        layoutInflater.inflate(
            R.layout.layout_custom_toast_error,
            findViewById(R.id.toast_message)
        )}

    override fun log(message: String) {
        Log.d("MyLog", message)
    }

}