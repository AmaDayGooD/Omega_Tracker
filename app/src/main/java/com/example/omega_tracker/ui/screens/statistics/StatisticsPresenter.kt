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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class StatisticsPresenter(private val settings:Settings) : BasePresenter<StatisticsView>() {

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    private var appRepository:AppRepository
    init {
        OmegaTrackerApp.appComponent!!.inject(this)
        appRepository = AppRepository(coroutineContext, retrofit, dataBaseTasks)
    }



    fun saveCurrentDisplay(value:Boolean){
        settings.saveCurrentDisplay(value)
    }

    fun getCurrentDisplay():Boolean{
        return settings.getCurrentDisplay()
    }

    fun getStatisticsForDay(){
        launch {
            val result = appRepository.getStatisticsToDay()
            viewState.setCurrentStatistics(calculateHourlyStatistics(result))
        }
    }
    private fun calculateHourlyStatistics(statisticsList: List<Statistics>): Map<String, Float> {
        val hourlyStatistics = mutableMapOf<String, Float>()

        val formatter = DateTimeFormatter.ofPattern("HH:00")

        for (statistic in statisticsList) {
            val hourKey = statistic.dataTimeCompleted.format(formatter)
            val spentTimeInMinutes = statistic.duration.inWholeMinutes
            hourlyStatistics[hourKey] = (hourlyStatistics.getOrDefault(hourKey, 0f) + spentTimeInMinutes)
        }

        for(hour in 8..17){
            val hourKey = String.format("%02d:00", hour)
            hourlyStatistics.putIfAbsent(hourKey, 0f)
        }
        val sortedHourlyStatistics = hourlyStatistics.toSortedMap(compareBy { it })
        log("$sortedHourlyStatistics")
        return sortedHourlyStatistics
    }

    private fun log(message:String){
        Log.d("MyLog","$message")
    }
}