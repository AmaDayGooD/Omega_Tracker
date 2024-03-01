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
import java.time.LocalDate
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
            viewState.addDateInListDate(LocalDateTime.now())
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
            getStatisticsToDay()
        }
    }

    fun getStatisticsToDay(toDay: LocalDateTime = LocalDateTime.now()) {
        launch {
            val result = appRepository.getStatisticsToDay(toDay)
            getSumSpentTime(result)
            if (toDay.toLocalDate() == LocalDate.now()) {
                viewState.setNumberOfCompletedTasks(result.size)
                viewState.setCurrentStatistics(calculateHourlyStatistics(result))
            } else {
                addNewData(toDay, result, false)
            }
        }
    }

    private fun getSumSpentTime(listStatistics: List<Statistics>) {
        var sumSpentTime = 0f
        listStatistics.forEach {
            sumSpentTime += it.duration.inWholeMinutes.toFloat()
        }
        viewState.setTimeSpent(sumSpentTime)
    }

//    fun setStartAndEndWeek(day: LocalDateTime):String {
//        val start = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
//        val end = day.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
//        return "$start - $end"
//    }

    fun getStatisticsToWeek(toDay: LocalDateTime = LocalDateTime.now()) {
        val startOfWeek =
            toDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0)
                .withSecond(0).withNano(0)
        val endOfWeek =
            toDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(23).withMinute(59)
                .withSecond(0).withNano(0)
        launch {
            val result = appRepository.getStatisticsToWeek(startOfWeek, endOfWeek)

            getSumSpentTime(result)
            if (toDay.toLocalDate() == LocalDate.now()) {
                viewState.setNumberOfCompletedTasks(result.size)
                viewState.setCurrentStatistics(calculateWeeklyStatistics(result))
            } else {
                addNewData(toDay, result, true)
            }

        }
    }

    private fun addNewData(toDay: LocalDateTime, result: List<Statistics>, type: Boolean) {
        if (type) {
            when {
                toDay > LocalDateTime.now() ->
                    viewState.addNextDayOrWeek(calculateWeeklyStatistics(result))

                toDay < LocalDateTime.now() ->
                    viewState.addPreviewDayOrWeek(calculateWeeklyStatistics(result))
            }
        } else {
            when {
                toDay > LocalDateTime.now() ->
                    viewState.addNextDayOrWeek(calculateHourlyStatistics(result))

                toDay < LocalDateTime.now() ->
                    viewState.addPreviewDayOrWeek(calculateHourlyStatistics(result))
            }
        }
    }

    private fun calculateHourlyStatistics(statisticsList: List<Statistics>): Map<String, Float> {
        val hourlyStatistics = mutableMapOf<String, Float>()

        val formatter = DateTimeFormatter.ofPattern("HH:00")
        statisticsList.forEach {
            val hourKey = it.dataTimeCompleted.format(formatter)
            val spentTimeInMinutes = it.duration.inWholeMinutes
            hourlyStatistics[hourKey] =
                hourlyStatistics.getOrDefault(hourKey, 0f) + spentTimeInMinutes
        }

        // Удаление крайних элементов с пустым значением value
        while (hourlyStatistics.isNotEmpty() && hourlyStatistics.values.firstOrNull() == 0f) {
            hourlyStatistics.remove(hourlyStatistics.keys.firstOrNull())
        }

        // Проверка на наличие минимального количества элементов
        if (hourlyStatistics.size in 1..3) {
            val lastItem = hourlyStatistics.keys.last()
            val firstItem = hourlyStatistics.keys.first()
            if (hourlyStatistics.keys.first() == "00:00") {
                var i = 1
                while (hourlyStatistics.size != 4) {
                    val substring = lastItem.substringBefore(":").toInt() + i
                    hourlyStatistics["$substring:00"] = 0f
                    i++
                }
            } else if (hourlyStatistics.keys.last() == "23:00") {
                var i = 1
                while (hourlyStatistics.size != 4) {
                    val substring = firstItem.substringBefore(":").toInt() - i
                    hourlyStatistics["$substring:00"] = 0f
                    i++
                }
            } else {
                var i = 1
                var state = false
                var substring = lastItem.substringBefore(":")
                while (hourlyStatistics.size != 4) {

                    var localSubstring: Int
                    if (state) {
                        localSubstring = substring.toInt() - i
                    } else {
                        localSubstring = substring.toInt() + i
                    }
                    if (localSubstring == 23) {
                        substring = firstItem.substringBefore(":")
                        state = true
                        i = 1
                    }
                    when (state) {
                        false -> hourlyStatistics["${localSubstring}:00"] = 0f
                        true -> hourlyStatistics["${localSubstring}:00"] = 0f
                    }
                    i++
                }
            }
        }

        // Построение полного списка ключей по возрастанию
        val sortedMap = hourlyStatistics.toSortedMap(compareBy { it.substringBefore(":").toInt() })
        val sortedKeys = sortedMap.keys.toList()
        val minKey = sortedKeys.firstOrNull()?.substringBefore(":")?.toInt() ?: 0
        val maxKey = sortedKeys.lastOrNull()?.substringBefore(":")?.toInt() ?: 23
        val completeKeys = (minKey..maxKey).map { String.format("%02d:00", it) }

        // Создание новой отсортированной map без пропусков
        val resultStatistics = mutableMapOf<String, Float>()
        for (key in completeKeys) {
            if (hourlyStatistics.containsKey(key)) {
                resultStatistics[key] = hourlyStatistics[key]!!
            } else {
                resultStatistics[key] = 0f
            }
        }

        // Если все значения map равны нулю, то создаём map с минимальным набором данных
        if (resultStatistics.all { it.value == 0f }) {
            resultStatistics.clear()
            resultStatistics["08:00"] = 0f
            resultStatistics["09:00"] = 0f
            resultStatistics["10:00"] = 0f
            resultStatistics["11:00"] = 0f
        }
        return resultStatistics
    }


    private fun calculateWeeklyStatistics(statisticsList: List<Statistics>): Map<String, Float> {
        val weeklyStatistics = mutableMapOf<String, Float>()

        // Добавляем все дни недели в словарь с начальным значением 0
        val daysOfWeek = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
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
        val saturdayValue = weeklyStatistics["СБ"]
        val sundayValue = weeklyStatistics["ВС"]

        when {
            saturdayValue == 0f && sundayValue != 0f -> weeklyStatistics["СБ"] = 0f
            saturdayValue != 0f && sundayValue == 0f -> weeklyStatistics.remove("ВС")
            saturdayValue == 0f && sundayValue == 0f -> {
                weeklyStatistics.remove("СБ")
                weeklyStatistics.remove("ВС")
            }
        }

        return weeklyStatistics
    }

    private fun log(message: String) {
        Log.d("MyLog", "$message")
    }
}