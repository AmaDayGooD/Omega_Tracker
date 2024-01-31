package com.example.omega_tracker.ui.screens.splash

import android.os.Bundle
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivitySplashBinding
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.example.omega_tracker.ui.screens.authorization.AuthorizationActivity.Companion.createIntent
import com.example.omega_tracker.ui.screens.main.MainActivity

class StartActivity : BaseActivity(R.layout.activity_splash), StartActivityView {
    lateinit var binding: ActivitySplashBinding

    override val presenter: StartPresenter by providePresenter{
        StartPresenter(Settings(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun gotoAuthorizationActivity() {
        startActivity(createIntent(this))
    }

    override fun gotoMainActivity(token: String?) {
        startActivity(MainActivity.createIntentMainActivity(this))
    }
}