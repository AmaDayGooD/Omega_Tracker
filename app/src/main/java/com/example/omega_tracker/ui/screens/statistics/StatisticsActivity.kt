package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import androidx.appcompat.widget.SwitchCompat
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivityStatisticsBinding
import com.example.omega_tracker.ui.base_class.BaseActivity
import kotlin.time.Duration.Companion.minutes

class StatisticsActivity : BaseActivity(R.layout.activity_statistics), StatisticsView,
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
    private var currentDisplay: Boolean = false


    private lateinit var chart: LineChartView
    private lateinit var switchDayAndWeek: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        switchDayAndWeek = binding.switchTypeDisplay
        chart = binding.chartStatistics
        chart.setListener(this)

        // Получение и восстановление сохранённого состояния показа дня или недели
        currentDisplay = presenter.getCurrentDisplay()
        switchDayAndWeek.isChecked = currentDisplay

        switchDayAndWeek.setOnClickListener {
            presenter.saveCurrentDisplay(switchDayAndWeek.isChecked)
            currentDisplay = presenter.getCurrentDisplay()
            presenter.showStatistics(currentDisplay)
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun setNumberOfCompletedTasks(number:Int){
        binding.numberOfCompletedTask.text = number.toString()
    }

    override fun setTimeSpent(value:Float){
        binding.textviewTimeSpent.text = createSpannableText(convertFloatToString(value))
    }

    override fun setCurrentStatistics(statistics: Map<String, Float>) {
        listX.clear()
        listY.clear()
        statistics.forEach { (str, lon) ->
            listX.add(str)
            listY.add(lon)
        }
        chart.setData(listX, listY,currentDisplay)
    }

    override fun getCurrentPosition(position: Int) {
        if (position < listY.size - 1) {
            val text = convertFloatToString(listY[position])
            binding.textviewTimeSpent.text =createSpannableText(text)
        }
    }
    private fun convertFloatToString(value: Float): String {
        val duration = value.toInt().minutes
        val hours = duration.inWholeHours.toInt()
        val remainingMinutes = duration.inWholeMinutes.toInt() % 60
        return String.format("%dч %dм", hours, remainingMinutes)
    }
    private fun createSpannableText(text:String): SpannableString {
        val indexFirstChar = text.indexOf('ч')
        val indexTwoChar = text.indexOf('м')
        val spannableString = SpannableString(text)
        spannableString.setSpan(AbsoluteSizeSpan(18,true),indexFirstChar,indexFirstChar+1,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(this.getColor(R.color.light_gray)),indexFirstChar,indexFirstChar+1,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(AbsoluteSizeSpan(18,true),indexTwoChar,indexTwoChar+1,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(this.getColor(R.color.light_gray)),indexTwoChar,indexTwoChar+1,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}