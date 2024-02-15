package com.example.omega_tracker.ui.screens.statistics

import android.util.Log
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.ui.base_class.BasePresenter
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import javax.inject.Inject

class StatisticsPresenter(private val settings: Settings) : BasePresenter<StatisticsView>() {

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    private var appRepository: AppRepository

    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        appRepository = AppRepository(coroutineContext, retrofit, dataBaseTasks)
        launch {
            showStatistics(getCurrentDisplay())
        }
    }

    fun saveCurrentDisplay(value: Boolean) {
        settings.saveCurrentDisplay(value)
    }

    fun getCurrentDisplay(): Boolean {
        return settings.getCurrentDisplay()
    }

    fun showStatistics(currentDisplay: Boolean) {
        if (currentDisplay) {
            getStatisticsToWeek()
        } else {
            getStatisticsForDay()
        }
    }

    private fun getStatisticsForDay() {
        launch {
            val result = appRepository.getStatisticsToDay()
            log("result ${calculateHourlyStatistics(result)}")
            getSumSpentTime(result)
            viewState.setNumberOfCompletedTasks(result.size)
            viewState.setCurrentStatistics(calculateHourlyStatistics(result))
        }
    }

    private fun getSumSpentTime(listStatistics: List<Statistics>) {
        var sumSpentTime = 0f
        listStatistics.forEach {
            sumSpentTime += it.duration.inWholeMinutes.toFloat()
        }
        viewState.setTimeSpent(sumSpentTime)
    }

    private fun getStatisticsToWeek() {
        val today = LocalDateTime.now()
        val startOfWeek =
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0)
                .withSecond(0).withNano(0)
        val endOfWeek =
            today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(23).withMinute(59)
                .withSecond(0).withNano(0)
        log("startOfWeek $startOfWeek || endOfWeek $endOfWeek")
        launch {
            val result = appRepository.getStatisticsToWeek(startOfWeek, endOfWeek)
            getSumSpentTime(result)
            viewState.setNumberOfCompletedTasks(result.size)
            viewState.setCurrentStatistics(calculateWeeklyStatistics(result))
        }
    }

    private fun calculateHourlyStatistics(statisticsList: List<Statistics>): Map<String, Float> {
        val hourlyStatistics = mutableMapOf<String, Float>()

        val formatter = DateTimeFormatter.ofPattern("HH:00")
        statisticsList.forEach {
            val hourKey = it.dataTimeCompleted.format(formatter)
            val spentTimeInMinutes = it.duration.inWholeMinutes
            hourlyStatistics[hourKey] =
                (hourlyStatistics.getOrDefault(hourKey, 0f) + spentTimeInMinutes)
        }

        for (hour in 8..17) {
            val hourKey = String.format("%02d:00", hour)
            hourlyStatistics.putIfAbsent(hourKey, 0f)
        }
        return hourlyStatistics.toSortedMap(compareBy { it })
    }

    private fun calculateWeeklyStatistics(statisticsList: List<Statistics>): Map<String, Float> {
        val weeklyStatistics = mutableMapOf<String, Float>()

        // Добавляем все дни недели в словарь с начальным значением 0
        val daysOfWeek = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ")
        daysOfWeek.forEach { day ->
            weeklyStatistics[day] = 0f
        }

        val formatter = DateTimeFormatter.ofPattern("EEE", Locale("ru"))
        statisticsList.forEach { statistic ->
            val dayKey = statistic.dataTimeCompleted.format(formatter).uppercase(Locale.ROOT)
            val spentTimeInMinutes = statistic.duration.inWholeMinutes.toFloat()
            weeklyStatistics[dayKey] =
                weeklyStatistics.getOrDefault(dayKey, 0f) + spentTimeInMinutes
        }
        return weeklyStatistics
    }

    private fun log(message: String) {
        Log.d("MyLog", "$message")
    }
}