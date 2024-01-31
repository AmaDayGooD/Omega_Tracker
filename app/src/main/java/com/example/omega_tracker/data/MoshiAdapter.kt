package com.example.omega_tracker.data

import com.squareup.moshi.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class ConvertCurrentTime

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class ConvertStartDate

class MoshiAdapter{

    @FromJson
    @ConvertStartDate
    fun startDate(value: String): LocalDateTime? {
        val date: Long = value.toLongOrNull() ?: 0L
        return if (date == 0L) LocalDateTime.now()
        else {
            Instant.ofEpochSecond(date).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }

    @FromJson
    @ConvertCurrentTime
    fun currentTime(value: String): Duration {
        return value.toDouble().toInt().minutes
    }
}