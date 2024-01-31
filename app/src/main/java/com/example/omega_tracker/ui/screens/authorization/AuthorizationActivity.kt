package com.example.omega_tracker.ui.screens.authorization

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivityAuthorizationBinding
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.example.omega_tracker.ui.screens.main.MainActivity

class AuthorizationActivity : BaseActivity(R.layout.activity_authorization), AuthorizationView {
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(
                context,
                AuthorizationActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    private lateinit var binding: ActivityAuthorizationBinding

    override val presenter: AuthorizationPresenter by providePresenter {
        AuthorizationPresenter(Settings(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Инициализация элементов binding
        val buttonAuth = binding.buttonAuthorization
        val textView = binding.inputEnterToken

        var token: String

        textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                setColorNormal()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setColorNormal()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        buttonAuth.setOnClickListener {
            buttonAuth.isEnabled = false
            token = textView.text.toString()
            presenter.auth(token)
            buttonAuth.isEnabled = true
        }
    }

    override fun saveToken(token: String) {
        val settings = Settings(this)
        settings.saveString(token)
    }

    override fun setButtonColorError() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity, R.drawable.bg_button_authorization_error
        )
        binding.textOnButtonAuthorization.setTextColor(
            ContextCompat.getColor(this@AuthorizationActivity, R.color.real_white)
        )
        binding.inputEnterToken.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.error_color)
        )
        animError()
    }

    override fun setButtonColorWarning() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity, R.drawable.bg_button_authorization_warning
        )
        binding.textOnButtonAuthorization.setTextColor(
            ContextCompat.getColor(this@AuthorizationActivity, R.color.real_white)
        )
        binding.inputEnterToken.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.toast_warning)
        )
        animError()
    }

    override fun setButtonColorRight() {
        binding.apply {
            buttonAuthorization.background = ContextCompat.getDrawable(
                this@AuthorizationActivity, R.drawable.bg_button_authorization_right
            )
            textOnButtonAuthorization.setTextColor(
                ContextCompat.getColor(
                    this@AuthorizationActivity, R.color.real_white
                )
            )
            textOnButtonAuthorization.text = getString(R.string.successfully)
            inputEnterToken.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@AuthorizationActivity, R.color.green
                )
            )
        }
    }

    override fun setColorNormal() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity, R.drawable.bg_button_authorization_normal
        )
        binding.inputEnterToken.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main))
    }

    override fun loadStart() {
        binding.progressBarLoading.visibility = ProgressBar.VISIBLE
        binding.textOnButtonAuthorization.visibility = TextView.GONE
        binding.buttonAuthorization.isClickable = false
    }

    override fun loadEnd() {
        binding.progressBarLoading.visibility = ProgressBar.GONE
        binding.textOnButtonAuthorization.visibility = TextView.VISIBLE
        binding.buttonAuthorization.isClickable = true

    }

    override fun tokenIsEmpty() {
        showToast(Constants.TOAST_TYPE_WARNING, R.string.enter_token)
        binding.buttonAuthorization.isEnabled = true
    }

    override fun tokenIsNotEmpty() {
        loadStart()
    }

    override fun checkInternet(token: String) {
        val connectionManager: ConnectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeInternet: NetworkInfo? = connectionManager.activeNetworkInfo
        if (activeInternet?.isConnected == true) {
            presenter.receivedBadAnswer()
        } else presenter.receiveNoInternet()


    }

    private fun animError() {
        val textView = binding.buttonAuthorization
        val animation = TranslateAnimation(-10f, 10f, 0f, 0f)
        animation.duration = 50
        animation.repeatCount = 3
        animation.repeatMode = Animation.REVERSE
        textView.startAnimation(animation)
    }

    override fun goMainScreen(token: String) {
        startActivity(MainActivity.createIntentMainActivity(this))
    }
}