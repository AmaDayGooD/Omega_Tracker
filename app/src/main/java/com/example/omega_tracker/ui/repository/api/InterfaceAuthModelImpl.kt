package com.example.omega_tracker.ui.repository.api

import android.util.Log
import com.example.omega_tracker.retrofit.interfaces.UserInterfase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InterfaceAuthModelImpl : InterfaceAuthModel {
    override suspend fun getDataFromApi(token: String): Boolean {

        val retrofit =
            Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val user = retrofit.create(UserInterfase::class.java)
        val test = user.getUserOne(token)
        Log.d("MyLog", "MVP = ${test.isSuccessful}")
        return test.isSuccessful
    }

    override suspend fun getNameUser(token: String): String {
        val retrofit =
            Retrofit.Builder().baseUrl("https://aleksandr152.youtrack.cloud")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val user = retrofit.create(UserInterfase::class.java)
        val test = user.getUserOne("Bearer $token")
        return test.body()?.name.toString()
    }

}