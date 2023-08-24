package com.example.omega_tracker

import android.util.Log
import kotlin.math.floor

class getMinuts {
    fun formatMinutes(minutes: String): String {
        val numbers: Double?
        return try {
            numbers = minutes.toDouble()
            val day = numbers / 1440

            val hours = (numbers / 60) % 24
            val remainingMinutes = numbers % 60
            //Log.d("MyLog",  "перевод $numbers | $day | $hours | $remainingMinutes" )
            when {
                floor(day).toInt()>0 -> "${floor(day).toInt()} д. ${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                floor(hours) > 0 -> "${floor(hours).toInt()} ч. ${floor(remainingMinutes).toInt()} м."
                else -> "${floor(remainingMinutes).toInt()} м."
            }
        } catch (e: NumberFormatException) {
            Log.d("MyLog", e.message.toString())
            "0"
        }
    }
}