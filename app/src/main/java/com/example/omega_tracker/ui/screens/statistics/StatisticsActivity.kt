package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.StatisticsData
import com.example.omega_tracker.databinding.ActivityStatisticsBinding
import com.example.omega_tracker.entity.Statistics
import com.example.omega_tracker.ui.base_class.BaseActivity

class StatisticsActivity() : BaseActivity(R.layout.activity_statistics), StatisticsView,
    ListenerMyProductivity {

    override val presenter: StatisticsPresenter by providePresenter {
        StatisticsPresenter(Settings(this))
    }

    companion object {
        fun createIntentStatisticsActivity(context: Context): Intent {
            return Intent(context, StatisticsActivity::class.java)
        }
    }

    private lateinit var binding: ActivityStatisticsBinding


    private var currentStatistics: Map<String, Float> = mapOf()
    private var currentDisplay: Boolean = false


    private lateinit var chart: LineChartView
    private lateinit var switchDayAndWeek: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        chart = binding.chartStatistics
        chart.setListener(this)

        switchDayAndWeek = binding.switchTypeDisplay

        if (currentDisplay) {
            // показываем неделю
            log("Неделя")
        } else {
            // Показываем день
            log("День")
            presenter.getStatisticsForDay()
            showChartDay()
        }


//        initChart()

        switchDayAndWeek.setOnClickListener {
            presenter.saveCurrentDisplay(switchDayAndWeek.isChecked)
            currentDisplay = presenter.getCurrentDisplay()
        }


        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun showChartDay() {

    }

    var listX: MutableList<String> = mutableListOf()
    var listY: MutableList<Float> = mutableListOf()
    private fun initChart() {
        listX = mutableListOf<String>(
            "8:00",
            "9:00",
            "10:00",
            "11:00",
            "12:00",
            "13:00",
            "14:00",
            "15:00",
            "16:00",
            "17:00"
        )
        listY = mutableListOf<Float>(45f, 10f, 67f, 42f, 0f, 10f, 60f, 42f, 25f, 45f, 0f)
        chart.setData(listX, listY)
    }

    override fun setCurrentStatistics(statistics: Map<String, Float>) {
        currentStatistics = statistics
        statistics.forEach{ (str, lon) ->
            listX.add(str)
            listY.add(lon)
            chart.setData(listX, listY)
        }
    }



    override fun getCurrentPosition(position: Int) {
        if (position < listY.size-1){
            binding.textviewTimeSpent.text = listY[position].toString()
        }
    }

}