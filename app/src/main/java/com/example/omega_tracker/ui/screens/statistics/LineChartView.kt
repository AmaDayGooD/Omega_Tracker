package com.example.omega_tracker.ui.screens.statistics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.omega_tracker.R
import java.lang.Float.*
import kotlin.math.abs

private const val MULTIPLIER_DAY = 2  // Модификатор расстояния меток для дня
private const val MULTIPLIER_WEEK = 4 // Модификатор расстояния меток для недели
private const val MULTIPLIER_LAST_POINT = .35f // Модификатор расстояния для последнего элемента
private const val LABEL_MARGIN = 150f // Ширина фиксированной области для меток по оси Y
private const val GENERAL_MARGIN = 20f
private const val BOTTOM_MARGIN_GRAPHIC = 120f
private const val MARGIN_FIRST_POINT = 70f

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
    private var listGradientColor = intArrayOf(
        context.getColor(R.color.main_strong), context.getColor(R.color.white)
    )

    fun setThicknessLine(thickness: Float) {
        if (thickness in 5f..15f) {
            thicknessLine = thickness
        }
    }

    /**
     * Настройка градиентного цвета линии графика
     *
     * параметря для двухцветного градиента
     * @param firstColor    первый цвет
     * @param twoColor      второй цвет
     */
    fun setGradientColorLine(firstColor: Int, twoColor: Int) {
        listGradientColor = intArrayOf(firstColor, twoColor)
    }

    /**
     * параметр для одноцветного линейного графика
     * @param singleColor цвет графика
     */
    fun setSingleColorLine(singleColor: Int) {
        listGradientColor = intArrayOf(singleColor, singleColor)
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
    private var listDays = listOf<String>()

    // Paint для рисования графика
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        isAntiAlias = true
        color = context.getColor(R.color.black)
    }

    private val paintBorderDay = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = 15f
        isAntiAlias = true
        //color = context.getColor(R.color.black)
    }

    private val paintTextBorderDay = Paint().apply {
        strokeWidth = 4f
        textSize = textXSize
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        color = context.getColor(R.color.light_gray)
        typeface = resources.getFont(R.font.rubik_medium)
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

    // Установка данных для графика
    private var horizontalOffset = 1f

    fun setListener(listener: ListenerMyProductivity) {
        listenerMyProductivity = listener
    }

    private var maxWidthText = 0f
    private var maxOffset = 0f
    private var xStep = 0f
    private var maxWidth = 0f

    private var countItems = mutableListOf<Int>()
    private fun resetSettingsToDefault() {
        // Сброс смещения в начальное положение
        // сброс координат точек
        listCoordinates.clear()
        stepY = if (typeView) 60f else 30f
        maxOffset = if (typeView) {
            ((xStep * (mDataX.size)) - (width - LABEL_MARGIN)) + (MULTIPLIER_LAST_POINT * xStep)
        } else {
            ((xStep * (mDataX.size)) - (width)) + (MULTIPLIER_LAST_POINT * xStep)
        }

        if (maxOffset < 0) {
            maxOffset = 1f
        }
    }

    var oldSize = 0
    private fun setXStep(size: Int) {
        maxWidthText = getMaxWidth()
        oldSize = kotlin.math.max(oldSize, size)
        xStep = if (typeView) {
            (width.toFloat()- LABEL_MARGIN) / oldSize
        } else {
            maxWidthText
        }
    }

    private fun getMaxWidth(): Float {
        val listWidth = mutableListOf<Float>()
        mDataX.forEach { text ->
            listWidth.add(paintTextXCoordinates.measureText(text))
        }

        return if (typeView) {
            listWidth.max()
        } else {
            (listWidth.max() * MULTIPLIER_DAY).toFloat()
        }
    }

    private fun resetHorizontalOffset() {
        horizontalOffset = 1f
    }

    fun setData(dataX: List<String>, dataY: List<Float>, displayType: Boolean) {
        // Тип показа день или неделя
        listDays = listOf()
        listDays = listenerMyProductivity?.getListDays()!!
        typeView = displayType
        mDataX = dataX
        mDataY = dataY
        countItems.clear()
        countItems.add(mDataX.size)
        setXStep(mDataX.size)
        resetHorizontalOffset()
        resetSettingsToDefault()
        invalidate()
    }

    fun addNewData(dataX: List<String>, dataY: List<Float>, size: Int = 0, type: Boolean) {
        mDataX = dataX
        mDataY = dataY
        listDays = listenerMyProductivity?.getListDays()!!
        setXStep(size)
        updateCountItems(type, size)
        resetSettingsToDefault()
        invalidate()
    }

    private fun updateCountItems(type: Boolean, size: Int) {
        // if the type is true, then we add a new day to the end,
        // otherwise we add a new day to the beginning of the list

        if (type) {
            countItems.add(mDataX.size)
        } else {
            countItems.reverse()
            for (i in 0 until countItems.size) {
                countItems[i] += size
            }
            countItems.add(size)
            countItems.reverse()
        }
    }

    private fun setHorizontalScroll(offset: Float) {
        if (offset.isInfinite()) {
            return
        }
        val newHorizontalOffset = max(0f, min(offset, maxOffset))
        if (newHorizontalOffset.isInfinite()) {
            return
        }
        horizontalOffset = newHorizontalOffset
        invalidate()
    }

    private fun drawShadowSelectPoint(canvas: Canvas) {
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
            height - BOTTOM_MARGIN_GRAPHIC,
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
        drawDayBorder(canvas)

        points.let {
            if (points != null) {
                drawShadowSelectPoint(canvas)
                drawGraph(canvas)
                drawSelectPoint(canvas)
            } else {
                drawGraph(canvas)
            }
        }
        points = null
        drawBackgroundYLabels(canvas)
        drawYAxisLabels(canvas)
    }

    private fun drawBackgroundYLabels(canvas: Canvas) {
        val cornerRadius = 20f // Радиус закругления углов
        val rectangle = RectF(
            0f,
            GENERAL_MARGIN, LABEL_MARGIN, height.toFloat() - GENERAL_MARGIN
        )
        canvas.drawRoundRect(rectangle, cornerRadius, cornerRadius, paintRectangle)
    }

    private fun drawBackground(canvas: Canvas) {
        // Рисование закругленного прямоугольника
        val cornerRadius = 20f // Радиус закругления углов
        val rect = RectF(
            0f,
            GENERAL_MARGIN, width.toFloat(), height.toFloat() - GENERAL_MARGIN
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paintRectangle)
    }


    private fun drawYAxisLabels(canvas: Canvas) {
        // Отрисовка меток по оси Y
        val intervals = ((mDataY.maxOrNull() ?: 0f) / stepY + 2).toInt()
        val intervalHeight = (height.toFloat() - 100) / intervals
        for (i in 0..intervals) {
            val label = formatTime(i * stepY.toInt())
            val yCoord = (height.toFloat() - 100 - i * intervalHeight) - GENERAL_MARGIN
            canvas.drawText(label, 0f + GENERAL_MARGIN, yCoord, paintTextYCoordinates)
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
        var xCord = 0f
        for (i in mDataX.indices) {
            // Проверка на бесконечное отрицательное значение horizontalOffset
            if (horizontalOffset.isInfinite()) {
                horizontalOffset = 0f
            }
            xCord =
                (LABEL_MARGIN + i * xStep - horizontalOffset)
            val label = mDataX[i]
            // Рисование текста на холсте
            canvas.drawText(
                label,
                xCord + MARGIN_FIRST_POINT,
                height.toFloat() - 50,
                paintTextXCoordinates
            )
        }
    }

    private fun drawDayBorder(canvas: Canvas) {
        // Подписи к границам дней

        val modifyCountItems = modifyList(countItems)
        for ((index, item) in modifyCountItems.withIndex()) {
            var offsetFirstItem = 0f
            if (index == 0) {
                offsetFirstItem = MARGIN_FIRST_POINT
            }
            val xCord = (LABEL_MARGIN + item * xStep - horizontalOffset)
            canvas.drawText(
                listDays[index],
                xCord + MARGIN_FIRST_POINT * 2,
                100f,
                paintTextBorderDay
            )

            if (index == 0) {
                continue // Пропуск этой итерации
            }

            val colors =
                intArrayOf(context.getColor(R.color.main), context.getColor(R.color.transparent))
            val gradients = LinearGradient(
                xCord - 200f + offsetFirstItem + MARGIN_FIRST_POINT,
                0f,
                xCord + offsetFirstItem + MARGIN_FIRST_POINT * 2,
                0f,
                colors,
                null,
                Shader.TileMode.CLAMP
            )
            paintBorderDay.strokeWidth = 200f
            paintBorderDay.shader = gradients

            canvas.drawLine(
                xCord + MARGIN_FIRST_POINT * 2,
                GENERAL_MARGIN,
                xCord + MARGIN_FIRST_POINT * 2,
                height - 120f,
                paintBorderDay
            )
        }
    }

    private fun modifyList(countItems: MutableList<Int>): MutableList<Int> {
        var result = mutableListOf<Int>()
        countItems.forEachIndexed { index, _ ->
            if (index == 0) {
                result.add(0)
            } else {
                result.add(countItems[index - 1])
            }
        }
        return result
    }

    private fun drawGraph(canvas: Canvas) {
        // Отрисовка графика
        // ПЕРЕДЕЛАТЬ!!  Метки для оси Y должны быть адаптивными
        val intervalHeight = (height.toFloat() - 100) / ((mDataY.maxOrNull() ?: 0f) / stepY + 2)
        val shader = LinearGradient(
            LABEL_MARGIN,
            height.toFloat() - 100f,
            width.toFloat() + 550f,
            height.toFloat(),
            listGradientColor,
            null,
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.shader = shader
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
        path.reset()
        for (i in mDataY.indices) {
            val xCoordinates = (LABEL_MARGIN + MARGIN_FIRST_POINT + (i * xStep) - horizontalOffset)
            val yCoordinates =
                height.toFloat() - BOTTOM_MARGIN_GRAPHIC - mDataY[i] * intervalHeight / stepY

            listCoordinates.add(Pair(LABEL_MARGIN + MARGIN_FIRST_POINT + (i * xStep), yCoordinates))
            if (listCoordinates.size > mDataX.size) {
                val subList = listCoordinates.subList(mDataX.size, listCoordinates.size)
                subList.clear()
            }

            if (i == 0) {
                path.moveTo(xCoordinates, yCoordinates)
            } else {
                val prevX = LABEL_MARGIN + MARGIN_FIRST_POINT + ((i - 1) * xStep) - horizontalOffset
                val prevY =
                    height.toFloat() - BOTTOM_MARGIN_GRAPHIC - mDataY[i - 1] * intervalHeight / stepY
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
                setHorizontalScroll(horizontalOffset - (offset / 20))
                onPress = offset < 1f
                addNewDay(offset)

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

    private fun addNewDay(offset: Float) {
        if (!typeView) {
            when {
                horizontalOffset == 0f && offset > width / 2 -> {
                    listenerMyProductivity!!.addPreviewDayOnGraphic()
                    val countStep = mDataX.size / 10
                    val step = maxOffset / countStep
                    horizontalOffset = step
                }
                horizontalOffset == maxOffset && offset < -(width / 2) -> {
                    listenerMyProductivity!!.addNextDayOnGraphic()
                }
            }
        } else {
            when {
                horizontalOffset == 0f && offset > width / 2 -> {
                    listenerMyProductivity!!.addPreviewWeekOnGraphic()
                    val countStep = mDataX.size / 7
                    val step = maxOffset / countStep
                    horizontalOffset = step
                }
                horizontalOffset == maxOffset && offset < -(width / 2) -> {
                    listenerMyProductivity!!.addNextWeekOnGraphic()
                }
            }
        }

    }

    private fun log(text: String) {
        Log.d("MyLog", "$text")
    }
}
