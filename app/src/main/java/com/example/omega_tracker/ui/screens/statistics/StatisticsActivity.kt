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

    private var listX: MutableList<String> = mutableListOf()
    private var listY: MutableList<Float> = mutableListOf()
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
        currentDisplay = presenter.getCurrentDisplay()
        switchDayAndWeek.isChecked = currentDisplay
        presenter.showStatistics(currentDisplay)

        switchDayAndWeek.setOnClickListener {
            presenter.saveCurrentDisplay(switchDayAndWeek.isChecked)
            currentDisplay = presenter.getCurrentDisplay()
            presenter.showStatistics(currentDisplay)

        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun setCurrentStatistics(statistics: Map<String, Float>) {
        listX.clear()
        listY.clear()
        statistics.forEach { (str, lon) ->
            listX.add(str)
            listY.add(lon)
            chart.setData(listX, listY, currentDisplay)
        }
        binding.numberOfCompletedTask.text = statistics.size.toString()
    }


    override fun getCurrentPosition(position: Int) {
        if (position < listY.size - 1) {
            binding.textviewTimeSpent.text = listY[position].toString()
        }
    }

}