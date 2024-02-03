package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.omega_tracker.R
import com.example.omega_tracker.databinding.ActivityStatisticsBinding
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class StatisticsActivity : BaseActivity(R.layout.activity_statistics), StatisticsView {

    companion object {
        fun createIntentStatisticsActivity(context: Context): Intent {
            return Intent(context, StatisticsActivity::class.java)
        }
    }

    override val presenter: StatisticsPresenter by providePresenter {
        StatisticsPresenter()
    }

    lateinit var binding: ActivityStatisticsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }
        initChart()

    }
    private fun initChart() {
        val chart = binding.chartStatistics
        val listX = listOf<String>("8:00","9:00","10:00","11:00","12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
        val listY = listOf<Int>(30,10,90,42,25,10,60,42,25,45)
        chart.setData(listX,listY)
    }

}