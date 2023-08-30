package com.example.omega_tracker.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.omega_tracker.ui.screens.main.MainActivity
import com.example.omega_tracker.databinding.ActivityAuthorizationBinding
import com.example.omega_tracker.ui.repository.api.InterfaceAuthModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthorizationActivity : AppCompatActivity(), AuthorizationView {
    lateinit var binding: ActivityAuthorizationBinding

    // инициализация MVP
    private lateinit var api: InterfaceAuthModelImpl
    private lateinit var authPresenter: AuthPresenter

    // инициализация Preferences
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // инициализация MVP
        authPresenter = AuthPresenter()
        api = InterfaceAuthModelImpl()

        // Инициализация элементов binding
        val buttonAuth = binding.authButton
        val textView = binding.enterToken

        this.preferences = getSharedPreferences("token", MODE_PRIVATE)
        var token: String

        buttonAuth.setOnClickListener {
            token = textView.text.toString()
            if (authPresenter.correctToken(textView.text.toString()))
                showToast("Введите токен")
            else {
                token = authPresenter.checkToken(token)

                CoroutineScope(Dispatchers.Main).launch {
                    val resultAuth = api.getDataFromApi(token)
                    if (resultAuth) {
                        authPresenter.saveToken(this@AuthorizationActivity,token)
                        gotoNextActivity(this@AuthorizationActivity, token)
                    } else {
                        withContext(Dispatchers.Main) {
                            showToast("Токен введён неверно\nпопробуйте снова $token")
                        }
                    }
                }
            }
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun gotoNextActivity(context: Context, token: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("token", token)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}





