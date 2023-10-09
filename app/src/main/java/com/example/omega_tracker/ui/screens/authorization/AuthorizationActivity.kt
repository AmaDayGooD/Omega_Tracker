package com.example.omega_tracker.ui.screens.authorization

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.repository.local_data.Settings
import com.example.omega_tracker.data.repository.local_data.Tasks_DAO
import com.example.omega_tracker.databinding.ActivityAuthorizationBinding
import com.example.omega_tracker.ui.screens.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Retrofit
import javax.inject.Inject

@AndroidEntryPoint
class AuthorizationActivity : AppCompatActivity(), AuthorizationView {
    companion object{
        fun createIntent(context: Context):Intent{
            return Intent(context, AuthorizationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    lateinit var binding: ActivityAuthorizationBinding

    @Inject
    lateinit var api: Retrofit
    @Inject
    lateinit var database: Tasks_DAO

    private lateinit var authPresenter: AuthorizationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // инициализация MVP
        authPresenter = AuthorizationPresenter(api, this,database)

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
            override fun afterTextChanged(s: Editable?) {
            }
        })

        buttonAuth.setOnClickListener {
            buttonAuth.isEnabled = false
            token = textView.text.toString()
            authPresenter.auth(token)
            buttonAuth.isEnabled = true
        }
    }

    override fun initCacheToken():Settings {
        return Settings(this)
    }

    override fun showToast(toastType: Int, message: Int) {
        val toast = layoutInflater.inflate(
            R.layout.layout_custom_toast_error,
            findViewById(R.id.toast_message)
        )
        val toastContainer = toast.findViewById<LinearLayout>(R.id.toast_message)
        val textMessage = toast.findViewById<TextView>(R.id.text_toast_message)

        when (toastType) {
            Constants.TOAST_TYPE_WARNING -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_warning)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_ERROR -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_error)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_SUCCESS -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_success)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_INFO -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_info)
                textMessage.text = getString(message)
            }
        }
        with(Toast(applicationContext))
        {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.CENTER, 0, 450)
            view = toast
            show()
        }
    }

    override fun log(message: String) {
        Log.d("MyLog", message)
    }

    override fun setButtonColorError() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity,R.drawable.bg_button_authorization_error
        )
        binding.textOnButtonAuthorization.setTextColor(
            ContextCompat.getColor(this@AuthorizationActivity,R.color.real_white)
        )
        binding.inputEnterToken.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error_color)
            )
        animError()
    }

    override fun setButtonColorWarning() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity,R.drawable.bg_button_authorization_warning
        )
        binding.textOnButtonAuthorization.setTextColor(
            ContextCompat.getColor(this@AuthorizationActivity,R.color.real_white)
        )
        binding.inputEnterToken.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.toast_warning)
            )
        animError()
    }

    override fun setButtonColorRight() {
        binding.apply {
            buttonAuthorization.background = ContextCompat.getDrawable(
                this@AuthorizationActivity,
                R.drawable.bg_button_authorization_right
            )
            textOnButtonAuthorization.setTextColor(
                ContextCompat.getColor(
                    this@AuthorizationActivity,
                    R.color.real_white
                )
            )
            textOnButtonAuthorization.text = getString(R.string.successfully)
            inputEnterToken.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@AuthorizationActivity,
                        R.color.green
                    )
                )
        }
    }

    override fun setColorNormal() {
        binding.buttonAuthorization.background = ContextCompat.getDrawable(
            this@AuthorizationActivity,
            R.drawable.bg_button_authorization_normal
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

    override fun checkInternet():Boolean{
        val connectionManager:ConnectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeInternet : NetworkInfo? = connectionManager.activeNetworkInfo
        return activeInternet?.isConnected ?: false
    }

    private fun animError() {
        val textView = binding.buttonAuthorization
        val animation = TranslateAnimation(-10f, 10f, 0f, 0f)
        animation.duration = 50
        animation.repeatCount = 3
        animation.repeatMode = Animation.REVERSE
        textView.startAnimation(animation)
    }

    override fun goMainScreen(token:String){
        startActivity(MainActivity.createIntent(this,token))
    }
}