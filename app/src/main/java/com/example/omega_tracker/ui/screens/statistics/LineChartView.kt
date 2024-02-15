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

private const val MULTIPLIER_DAY = 2  // Модификатор расстояния меток для дня
private const val MULTIPLIER_WEEK = 4 // Модификатор расстояния меток для недели
private const val MULTIPLIER_LAST_POINT = .2f // Модификатор расстояния для последнего элемента
private const val LABEL_MARGIN = 150f // Ширина фиксированной области для меток по оси Y

class LineChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /**
     * Параметры подписей осей
     *
     * @param textXSize размер подписей для оси X
     * @param textYSize размер подписей для оси Y
     * @param textXColor цвет подписей для оси X
     * @param textYColor цвет подписей для оси Y
     */


    private var textXSize = 40f
    private var textYSize = 30f
    private var textXColor = 1
    private var textYColor = 2

    fun setTextXSize(size: Float) {
        if (size in 25f..45f) {
            textXSize = size
        }
    }

    fun setTextYSize(size: Float) {
        if (size in 25f..45f) {
            textYSize = size
        }
    }

    /**
     * Параметры линейного графика
     *
     * @param thicknessLine толщина линии графика
     */

    private var thicknessLine = 10f

    fun setThicknessLine(thickness: Float) {
        if (thickness in 5f..15f) {
            thicknessLine = thickness
        }
    }


    private var mDataX = emptyList<String>() // Массив времени событий
    private var mDataY = emptyList<Float>() // Массив времени, потраченного на события (в минутах)
    private val path = Path()
    private var listenerMyProductivity: ListenerMyProductivity? = null
    private val listCoordinates = mutableListOf<Pair<Float, Float>>()
    private var lastY = 0f
    private var indexPoint = 0
    private var typeView: Boolean = false
    private var stepY: Float = 30f

    // Paint для рисования графика
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    // Paint's для рисования текста
    private val paintTextXCoordinates = Paint().apply {
        strokeWidth = 4f
        textSize = textXSize
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.light_gray)
        typeface = resources.getFont(R.font.rubik_medium)
    }
    private val paintTextYCoordinates = Paint().apply {
        strokeWidth = 4f
        textSize = textYSize
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
        color = context.getColor(R.color.light_gray)
        typeface = resources.getFont(R.font.rubik_regular)
    }

    // Paint для рисования горизонтальных линий
    private val paintHorizontalLine = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = context.getColor(R.color.semi_transparent_black)
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(20f, 25f), 0f)
    }

    // Paint для выделения времени
    private val paintCircle = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.main)
    }
    private var paintShadow = Paint()

    // Paint для заливки заднего фона
    private val paintRectangle = Paint().apply {
        color = context.getColor(R.color.real_white) // Цвет прямоугольника
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

    private var bottomMarginGraphic = 120f
    private var maxWidthText = 0f
    private var maxOffset = 0f
    private var xStep = 0f
    private var xMax = 0f

    private fun resetSettingsToDefault() {
        //Сброс смещения в начальное положение
        horizontalOffset = 0f
        // сброс координат точек
        listCoordinates.clear()

        stepY = if (typeView) 60f else 30f
        maxWidthText = getMaxWidth()
        xStep = maxWidthText
        maxOffset = ((xStep * (mDataX.size)) - (width)) + MULTIPLIER_LAST_POINT * xStep
        log("maxOffset $maxOffset")
    }

    fun setData(dataX: List<String>, dataY: List<Float>, displayType: Boolean) {
        // Тип тип показа деньили неделя
        typeView = displayType
        mDataX = dataX
        mDataY = dataY
        resetSettingsToDefault()
        invalidate()
    }

    private fun setHorizontalScroll(offset: Float) {
        horizontalOffset = max(0f, min(offset, maxOffset))
        invalidate()
    }

    private fun drawShadow(canvas: Canvas) {
        paintShadow.shader = LinearGradient(
            (points!!.first - 50f) - horizontalOffset,
            lastY,
            (points!!.first + 50f) - horizontalOffset,
            height - 100f,
            context.getColor(R.color.semitransparent_light_purple_25),
            context.getColor(R.color.semitransparent_light_purple_100),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            (points!!.first - 50f) - horizontalOffset,
            abs(lastY),
            (points!!.first + 50f) - horizontalOffset,
            height - 120f,
            paintShadow
        )
    }

    private fun drawSelectPoint(canvas: Canvas) {
        canvas.drawCircle(
            points!!.first - horizontalOffset,
            points!!.second,
            20f,
            paintCircle
        )
        paintCircle.color = context.getColor(R.color.real_white)
        canvas.drawCircle(
            points!!.first - horizontalOffset,
            points!!.second,
            10f,
            paintCircle
        )
        paintCircle.color = context.getColor(R.color.main_strong)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawXAxisLabels(canvas)

        points.let {
            if (points != null) {
                drawShadow(canvas)
                drawGraph(canvas)
                drawSelectPoint(canvas)
            } else {
                drawGraph(canvas)
            }
        }
        points = null
        test(canvas)
        drawYAxisLabels(canvas)
    }

    private fun test(canvas: Canvas) {
        val rectangle = RectF(0f, 0f, LABEL_MARGIN, height.toFloat())
        canvas.drawRect(rectangle, paintRectangle)
    }

    private fun drawBackground(canvas: Canvas) {
        // Рисование закругленного прямоугольника
        val cornerRadius = 20f // Радиус закругления углов
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paintRectangle)
    }

    private fun getMaxWidth(): Float {
        val listWidth = mutableListOf<Float>()
        mDataX.forEach { text ->
            listWidth.add(paintTextXCoordinates.measureText(text))
        }
        return if (typeView) {
            listWidth.max() * MULTIPLIER_WEEK
        } else {
            listWidth.max() * MULTIPLIER_DAY
        }

    }

    private fun drawYAxisLabels(canvas: Canvas) {
        // Отрисовка меток по оси Y
        val intervals = ((mDataY.maxOrNull() ?: 0f) / stepY + 2).toInt()
        val intervalHeight = (height.toFloat() - 100) / intervals
        for (i in 0..intervals) {
            val label = formatTime(i * stepY.toInt())
            val yCoord = (height.toFloat() - 100 - i * intervalHeight) - margin
            canvas.drawText(label, 0f + margin, yCoord, paintTextYCoordinates)
            canvas.drawLine(
                width.toFloat() - 30f,
                yCoord,
                LABEL_MARGIN,
                yCoord,
                paintHorizontalLine
            )
            if (i == intervals - 1)
                lastY = yCoord
        }
    }

    // Отрисовка меток по оси X
    private fun drawXAxisLabels(canvas: Canvas) {
        for (i in mDataX.indices) {
            val xCoord =
                (LABEL_MARGIN + i * xStep - horizontalOffset) //5f для сопоставления точки на графике и подписи
            val label = mDataX[i]
            // Рисование текста на холсте
            canvas.drawText(label, xCoord + 70f, height.toFloat() - 50, paintTextXCoordinates)
        }

    }

    private fun drawGraph(canvas: Canvas) {
        // Отрисовка графика
        val intervalHeight = (height.toFloat() - 100) / ((mDataY.maxOrNull() ?: 0f) / stepY + 2)
        val colors = intArrayOf(
            context.getColor(R.color.main_strong), resources.getColor(R.color.white)
        )
        val shader = LinearGradient(
            LABEL_MARGIN,
            height.toFloat() - 100f,
            width.toFloat() + 550f,
            height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.shader = shader
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
        path.reset()
        for (i in mDataY.indices) {
            val xCoordinates = (LABEL_MARGIN + 70f + (i * xStep) - horizontalOffset)
            val yCoordinates =
                height.toFloat() - bottomMarginGraphic - mDataY[i] * intervalHeight / stepY
            listCoordinates.add(Pair(xCoordinates - horizontalOffset, yCoordinates))
            if (listCoordinates.size > mDataX.size) {
                val subList = listCoordinates.subList(mDataX.size, listCoordinates.size)
                subList.clear()
            }
            if (i == 0) {
                path.moveTo(xCoordinates, yCoordinates)
            } else {
                val prevX = LABEL_MARGIN + 70f + ((i-1) * xStep) - horizontalOffset
                val prevY =
                    height.toFloat() - bottomMarginGraphic - mDataY[i-1] * intervalHeight / stepY
                val cx = (prevX + xCoordinates) / 2
                path.cubicTo(cx, prevY, cx, yCoordinates, xCoordinates, yCoordinates)
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
                        if (event.x + horizontalOffset in (x - 50)..(x + 50) && event.y in (y - 50)..(y + 50)) {
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
