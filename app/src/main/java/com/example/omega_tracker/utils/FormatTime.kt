package com.example.omega_tracker.utils

import android.util.Log
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object FormatTime {
    fun formatSeconds(seconds: Duration): String {
        val days = seconds.inWholeDays
        val hours = seconds.minus(days.days).inWholeHours
        val minutes = (seconds.minus(days.days)).minus(hours.hours).inWholeMinutes
        val seconds = ((seconds.minus(days.days)).minus(hours.hours)).minus(minutes.minutes).inWholeSeconds
        return when {
            abs(days) > 0 -> "${days}д. ${hours}ч."
            abs(hours) > 0 -> "${hours}ч ${minutes}м."
            abs(minutes) > 0 -> "${minutes}м. ${seconds}с."
            else -> "${seconds}с."
        }
    }
}