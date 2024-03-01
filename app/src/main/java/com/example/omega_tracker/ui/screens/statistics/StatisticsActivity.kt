package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivityStatisticsBinding
import com.example.omega_tracker.ui.base_class.BaseActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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

    // Список дней отображённых на графике
    private var listDate = mutableListOf<LocalDateTime>()
    private var listWeek = mutableListOf<LocalDateTime>()
    private var saveIndex = -1
    private lateinit var chart: LineChartView
    private lateinit var switchDayAndWeek: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        switchDayAndWeek = binding.switchTypeDisplay
        chart = binding.chartStatistics
        chart.setListener(this)

        val currentDate = "${convertData(LocalDateTime.now())} (сегодня)"
        binding.textviewDayShown.text = currentDate
        binding.textviewDayShown.setTextColor(this.getColor(R.color.main))

        // Получение и восстановление сохранённого состояния показа дня или недели
        currentDisplay = presenter.getCurrentDisplay()
        switchDayAndWeek.isChecked = currentDisplay

        if (currentDisplay) {
            binding.textviewDayShown.visibility = View.GONE
        }

        switchDayAndWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                presenter.saveCurrentDisplay(true)
                currentDisplay = true
                presenter.showStatistics(currentDisplay)
                binding.textviewDayShown.visibility = View.GONE
            } else {
                presenter.saveCurrentDisplay(false)
                currentDisplay = false
                presenter.showStatistics(currentDisplay)
                binding.textviewDayShown.apply {
                    visibility = View.VISIBLE
                    text = currentDate
                }
            }
        }


        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun addDateInListDate(day: LocalDateTime) {
        listDate.add(day)
    }

    override fun setNumberOfCompletedTasks(number: Int) {
        binding.numberOfCompletedTask.text = number.toString()
    }

    override fun setTimeSpent(value: Float) {
        binding.textviewTimeSpent.text = createSpannableText(convertFloatToString(value))
    }


    override fun setCurrentStatistics(statistics: Map<String, Float>) {
        listX.clear()
        listY.clear()
        listDate.clear()
        addDateInListDate(LocalDateTime.now())
        statistics.forEach { (str, lon) ->
            listX.add(str)
            listY.add(lon)
        }
        chart.setData(listX, listY, currentDisplay)
    }

    override fun getCurrentPosition(position: Int) {
        if (position < listY.size - 1) {
            val text = convertFloatToString(listY[position])
            binding.textviewTimeSpent.text = createSpannableText(text)
        }
    }

    private fun convertFloatToString(value: Float): String {
        val duration = value.toInt().minutes
        val hours = duration.inWholeHours.toInt()
        val remainingMinutes = duration.inWholeMinutes.toInt() % 60
        return String.format("%dч %dм", hours, remainingMinutes)
    }

    private fun createSpannableText(text: String): SpannableString {
        val indexFirstChar = text.indexOf('ч')
        val indexTwoChar = text.indexOf('м')
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            AbsoluteSizeSpan(18, true),
            indexFirstChar,
            indexFirstChar + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(this.getColor(R.color.light_gray)),
            indexFirstChar,
            indexFirstChar + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            AbsoluteSizeSpan(18, true),
            indexTwoChar,
            indexTwoChar + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(this.getColor(R.color.light_gray)),
            indexTwoChar,
            indexTwoChar + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    override fun addNextDayOrWeek(nextWeek: Map<String, Float>) {
        nextWeek.forEach { (str, lon) ->
            listX.add(str)
            listY.add(lon)
        }
        chart.addNewData(listX, listY, type = true)
    }

    override fun addPreviewDayOrWeek(previewDay: Map<String, Float>) {
        // Создаем список для временного хранения новых элементов
        val tempListX = mutableListOf<String>()
        val tempListY = mutableListOf<Float>()

        // Добавляем новые элементы из previewDay в начало временных списков
        previewDay.entries.reversed().forEach { (str, lon) ->
            tempListX.add(0, str)
            tempListY.add(0, lon)
        }

        // Добавляем остальные элементы из listX и listY в конец временных списков
        tempListX.addAll(listX)
        tempListY.addAll(listY)

        // Обновляем списки listX и listY
        listX.clear()
        listY.clear()
        listX.addAll(tempListX)
        listY.addAll(tempListY)
        val amountHours = previewDay.size
        // Обновляем данные в графике
        chart.addNewData(listX, listY, amountHours, false)
    }

    override fun addNextWeekOnGraphic() {
        val nextWeek = listDate[listDate.size - 1].plusWeeks(1)
        listDate.add(nextWeek)
        presenter.getStatisticsToWeek(nextWeek)
    }

    override fun addPreviewWeekOnGraphic() {
        val previewDay = listDate[0].minusWeeks(1)
        listDate.add(0, previewDay)
        presenter.getStatisticsToWeek(previewDay)
    }


    override fun addNextDayOnGraphic() {
        val nextDay = listDate[listDate.size - 1].plusDays(1)
        listDate.add(nextDay)
        presenter.getStatisticsToDay(nextDay)
    }

    override fun addPreviewDayOnGraphic() {
        val previewDay = listDate[0].minusDays(1)
        listDate.add(0, previewDay)
        presenter.getStatisticsToDay(previewDay)
    }

    override fun getListDays(): List<String> {
        val result = mutableListOf<String>()
        listDate.forEach {
            result.add(convertData(it))
        }
        return result
    }


    override fun setCurrentDay(indexCurrentDay: Int) {
        if (saveIndex != indexCurrentDay) {
            val currentLocalDataTime = listDate[indexCurrentDay]
            var currentDate = convertData(currentLocalDataTime)
            if (currentLocalDataTime.toLocalDate() == LocalDate.now()) {
                binding.textviewDayShown.setTextColor(this.getColor(R.color.main))
                currentDate = "$currentDate (сегодня)"
            } else
                binding.textviewDayShown.setTextColor(this.getColor(R.color.light_gray))
            binding.textviewDayShown.text = currentDate
            saveIndex = indexCurrentDay
        }
    }

    private fun convertData(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale("ru"))
        return dateTime.format(formatter)
    }
}