package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.omega_tracker.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class LineChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDataX = emptyList<String>() // Массив времени событий
    private var mDataY = emptyList<Int>() // Массив времени, потраченного на события (в минутах)
    private val path = Path()

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        strokeWidth = 4f
        textSize = 24f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Установка данных для графика
    fun setData(dataX: List<String>, dataY: List<Int>) {
        mDataX = dataX
        mDataY = dataY
        invalidate() // Перерисовываем представление
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()


        // Рисуем метки времени по Y
        val maxY = mDataY.maxOrNull() ?: 0
        val intervals = (maxY / 30)+2 // Количество интервалов на оси Y (по 30 минут)
        val intervalHeight = (height - 100) / intervals // Высота каждого интервала на графике

        for (i in 0..intervals) {
            val label = formatTime(i * 30) // Преобразуем значение интервала в удобный формат
            val yCoord = height - 100 - i * intervalHeight
            canvas.drawText(label, 20f, yCoord, paintText)
        }

        // Рисуем метки времени по X
        val xStep = (width - 200) / (mDataX.size - 1)
        for (i in mDataX.indices) {
            val xCoord = 70 + i * xStep
            val label = mDataX[i]
            canvas.drawText(label, xCoord, height - 50, paintText)
        }

        // Рисуем линию графика
        val colors = intArrayOf(
            resources.getColor(R.color.main),
            resources.getColor(R.color.real_white)
        )
        val shader =
            LinearGradient(200f, height - 100f, width, height, colors, null, Shader.TileMode.CLAMP)
        paint.style = Paint.Style.STROKE
        paint.shader = shader
        paint.strokeWidth = 7f
        path.reset()
        for (i in mDataY.indices) {
            val xCoord = 70 + i * xStep
            val yCoord = height - 100 - mDataY[i] * intervalHeight / 30
            if (i == 0) {
                path.moveTo(xCoord, yCoord)
            } else {
                val prevX = 70 + (i - 1) * xStep
                val prevY = height - 100 - mDataY[i - 1] * intervalHeight / 30
                val cx = (prevX + xCoord) / 2
                path.cubicTo(cx, prevY, cx, yCoord, xCoord, yCoord)
            }
        }
        canvas.drawPath(path, paint)
    }

    // Метод для форматирования времени в удобный формат (часы и минуты)
    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val remainderMinutes = minutes % 60
        return if (hours == 0) {
            "${remainderMinutes}м"
        } else {
            "${hours}ч${remainderMinutes}м"
        }
    }

    private fun addMinutes(text: String): String {
        val modifiedText = text.replace("д", "d").replace("ч", "h").replace("м", "m")
        val duration = Duration.parse(modifiedText) + 30.minutes
        return duration.toString().replace("d", "д").replace("h", "ч").replace("m", "м")
    }


//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                val x = event.x
//                val y = event.y
//                // Проходим по каждой точке на графике и проверяем, попадает ли точка касания в окрестность
//                mData.forEach { (dataX, dataY) ->
//                    val xStep = (width - 100) / (mData.size - 1)
//                    val yStep = (height - 100) / (mData.maxByOrNull { it.second }?.second ?: 1f)
//                    val xCoord = 100 + dataX * xStep
//                    val yCoord = height - 100 - dataY * yStep
//                    val radius = 50f // Радиус области для нажатия
//                    // Проверяем, попадает ли точка касания в окрестность текущей точки на графике
//                    if (x >= xCoord - radius && x <= xCoord + radius && y >= yCoord - radius && y <= yCoord + radius) {
//                        Log.d("LineChartView", "Pressed on point: ($dataX, $dataY)")
//                        return true // Обработка события завершена
//                    }
//                }
//            }
//        }
//        return super.onTouchEvent(event)
//    }


}

