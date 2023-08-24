package com.example.omega_tracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import com.example.omega_tracker.databinding.ActivityAuthorizationBinding
import com.example.omega_tracker.retrofit.interfaces.UserInterfase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

class AuthorizationActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthorizationBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val helpForToken = binding.helpForToken
        val buttonAuth = binding.authButton
        val textView = binding.enterToken


        // OKHTTP
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


        helpForToken.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.jetbrains.com/help/youtrack/devportal/Manage-Permanent-Token.html#new-permanent-token")
            )
            startActivity(intent)
        }
        val retrofit =
            Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud").client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()

        val user = retrofit.create(UserInterfase::class.java)

        this.preferences = getSharedPreferences("token", MODE_PRIVATE)
        var enteredToken = preferences.getString("token", "").toString()

        buttonAuth.setOnClickListener {
            enteredToken = textView.text.toString()
            if (enteredToken.isEmpty()) {
                Toast.makeText(this, "Введите токен", Toast.LENGTH_SHORT).show()
            } else {
                if (!(enteredToken.contains("perm:"))) {
                    enteredToken = "Bearer perm:$enteredToken"
                    textView.text = enteredToken.toEditable()
                }
            }


            // Retrofit
            CoroutineScope(Dispatchers.IO).launch {
                if (!enteredToken.contains("Bearer")) {
                    enteredToken = "Bearer $enteredToken"
                }
                val oneUser = user.getUserOne(enteredToken)
                runOnUiThread {
                    if (oneUser.isSuccessful) {
                        val saver =
                            SaveAccessToken(this@AuthorizationActivity, enteredToken)
                        saver.save()
                        gotoNextActivity(this@AuthorizationActivity, enteredToken)
                    } else {
                        Toast.makeText(this@AuthorizationActivity, "Возникла ошибка", Toast.LENGTH_SHORT).show()
                        textView.text.clear()
                    }
                }
            }
        }
    }
}


class SaveAccessToken(private val context: Context, private val token: String) {
    fun save() {
        val preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("token", token)
        editor.apply()
    }
}


fun gotoNextActivity(context: Context, token: String) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("token", token)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

