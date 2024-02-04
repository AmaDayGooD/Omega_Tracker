package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.omega_tracker.R
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs

class LineChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDataX = emptyList<String>() // Массив времени событий
    private var mDataY = emptyList<Float>() // Массив времени, потраченного на события (в минутах)
    private val path = Path()
    private var listenerMyProductivity: ListenerMyProductivity? = null
    private val listCoordinates = mutableListOf<Pair<Float, Float>>()
    private var lastY = 0f
    private var indexPoint = 0
    private var typeView: Boolean = false
    private var stepY :Float = 30f

    // Paint для рисования графика
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    // Paint's для рисования текста
    private val paintTextXCoordinates = Paint().apply {
        strokeWidth = 4f
        textSize = 40f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = resources.getColor(R.color.light_gray)
        typeface = resources.getFont(R.font.rubik_regular)
    }
    private val paintTextYCoordinates = Paint().apply {
        strokeWidth = 4f
        textSize = 40f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
        color = resources.getColor(R.color.light_gray)
        typeface = resources.getFont(R.font.rubik_regular)
    }

    // Paint для рисования горизонтальных линий
    private val paintHorisontalLine = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = resources.getColor(R.color.semi_transparent_black)
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(20f, 25f), 0f)
    }

    // Paint для выделения времени
    private val paintCircle = Paint().apply {
        style = Paint.Style.FILL
        color = resources.getColor(R.color.main)
    }
    private var paintShadow = Paint()

    // Paint для заливки заднего фона
    private val paintRectangle = Paint().apply {
        color = resources.getColor(R.color.real_white) // Цвет прямоугольника
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Margin всего элемента
    private val margin = 20f

    // Установка данных для графика
    private var horizontalOffset = 0f

    fun setListener(listener: ListenerMyProductivity) {
        listenerMyProductivity = listener
    }

    fun setData(dataX: List<String>, dataY: List<Float>, displayType: Boolean) {
        listCoordinates.clear()
        typeView = displayType
        stepY = 30f
        if(typeView){
            stepY = 60f
        }
        mDataX = dataX
        mDataY = dataY
        invalidate()
    }

    private fun setHorizontalScroll(offset: Float) {
//        var maxOffset = max(0f, width.toFloat() - 76f) // нормальынй максимальный оффсет для реального телефона
        var maxOffset = max(0f, width.toFloat() - 26f)
        // если FALSE то показывается статистика за неделю, иначе статистика за день
        if(typeView){
            //maxOffset = max(0f, width.toFloat())-560f  // нормальынй оффсет для реального телефона
            maxOffset = max(0f, width.toFloat())-400f
        }
        horizontalOffset = max(0f, min(offset, maxOffset))

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисование закругленного прямоугольника
        val cornerRadius = 20f // Радиус закругления углов
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paintRectangle)

        val width = width.toFloat() - 5f
        val height = height.toFloat() - 5f

        val labelMargin = 150f // Ширина фиксированной области для меток по оси Y

        val maxY = mDataY.maxOrNull() ?: 0f
        val intervals = ((maxY / stepY) + 2).toInt()
        val intervalHeight = (height - 100) / intervals

        // Отрисовка меток по оси Y
        for (i in 0..intervals) {
            val label = formatTime(i * stepY.toInt())
            val yCoord = (height - 100 - i * intervalHeight) - margin
            canvas.drawText(label, 0f + margin, yCoord, paintTextYCoordinates)
            canvas.drawLine(width - 30f, yCoord, labelMargin, yCoord, paintHorisontalLine)
            if (i == intervals - 1)
                lastY = yCoord
        }

        // Отрисовка меток по оси X
        val xStep = ((width - labelMargin) / (mDataX.size - 1)) + 76f
        for (i in mDataX.indices) {
            val xCoord =
                ((labelMargin - margin) + i * xStep - horizontalOffset) + 20f //5f для сопоставления точки на графике и подписи
            val label = mDataX[i]
            canvas.drawText(label, xCoord+50f, height - 50, paintTextXCoordinates)
        }

        // Отрисовка графика
        val colors = intArrayOf(
            resources.getColor(R.color.main_strong), resources.getColor(R.color.white)
        )
        val shader = LinearGradient(
            labelMargin,
            height - 100f,
            width + 550f,
            height,
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.shader = shader
        paint.strokeWidth = 10f
        path.reset()
        for (i in mDataY.indices) {
            val xCoord = (labelMargin + i * xStep - horizontalOffset)+50f
            val yCoord = height - 120 - mDataY[i] * intervalHeight / stepY
            listCoordinates.add(Pair(xCoord - horizontalOffset, yCoord + horizontalOffset))
            if (i == 0) {
                path.moveTo(xCoord, yCoord)
            } else {
                val prevX = labelMargin + (i - 1) * xStep - horizontalOffset
                val prevY = height - 120 - mDataY[i - 1] * intervalHeight / stepY
                val cx = (prevX + xCoord) / 2
                path.cubicTo(cx, prevY, cx, yCoord, xCoord, yCoord)
            }
        }

        points.let {
            if (points != null && indexPoint < mDataX.size) {
                // Рисуем тень на выбранном времени
                paintShadow.shader = LinearGradient(
                    (points!!.first - 50f) - horizontalOffset,
                    lastY,
                    (points!!.first + 50f) - horizontalOffset,
                    height - 100f,
                    getResources().getColor(R.color.semitransparent_light_purple_25),
                    getResources().getColor(R.color.semitransparent_light_purple_100),
                    Shader.TileMode.CLAMP
                )

                canvas.drawRect(
                    (points!!.first - 50f) - horizontalOffset,
                    abs(lastY),
                    (points!!.first + 50f) - horizontalOffset,
                    height - 120f,
                    paintShadow
                )
                // Рисуем точку на выбранном времени
                canvas.drawCircle(
                    points!!.first - horizontalOffset,
                    points!!.second,
                    20f,
                    paintCircle
                )
                paintCircle.color = resources.getColor(R.color.real_white)
                canvas.drawCircle(
                    points!!.first - horizontalOffset,
                    points!!.second,
                    10f,
                    paintCircle
                )
                paintCircle.color = resources.getColor(R.color.main_strong)


            }
        }
        canvas.drawPath(path, paint)
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val remainderMinutes = minutes % 60
        return "${hours}ч${remainderMinutes}м"
    }

    private var previousX = 0f
    private var points: Pair<Float, Float>? = null

    private var onPress = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                onPress = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val offset = event.x - previousX
                setHorizontalScroll(horizontalOffset - offset / 20)
                onPress = offset < 1f
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (onPress) {
                    listCoordinates.forEachIndexed { index, (x, y) ->
                        if (event.x + horizontalOffset in (x - 40)..(x + 40) && event.y in (y - 40)..(y + 40)) {
                            points = Pair(x, y)
                            indexPoint = index
                            listenerMyProductivity!!.getCurrentPosition(index)
                            return true
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun log(text: String) {
        Log.d("MyLog", "$text")
    }
}