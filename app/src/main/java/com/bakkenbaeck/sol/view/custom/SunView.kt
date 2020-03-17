package com.bakkenbaeck.sol.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.bakkenbaeck.sol.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SunView : View {

    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.sun_default_color)
            textSize = resources.getDimensionPixelSize(R.dimen.sun_text_size).toFloat()
        }
    }

    private var startLabel: String = ""
    private var endLabel: String = ""
    private var floatingLabel: String = ""

    private val viewRect by lazy { Rect() }

    private var textMargin = 0f
    private var horizon_bottom_margin = 0
    private var circleRadius = 0
    private var availableViewWidth = 0
    private var strokeWidth = 0
    private var rightMarginLabel = 0f
    private var leftMarginLabel = 0f
    private val sunCoordinates: PointF by lazy { PointF(.0f, .0f) }
    private var sunIsVisible = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        val resources = context.resources
        textMargin = resources.getDimensionPixelSize(R.dimen.sun_text_margin).toFloat()
        horizon_bottom_margin = resources.getDimensionPixelSize(R.dimen.sun_horizon_bottom_margin)
        circleRadius = resources.getDimensionPixelSize(R.dimen.sun_radius)
        strokeWidth = resources.getDimensionPixelSize(R.dimen.sun_stroke_width)
        rightMarginLabel = resources.getDimensionPixelSize(R.dimen.sun_right_margin_label).toFloat()
        leftMarginLabel = resources.getDimensionPixelSize(R.dimen.sun_left_margin_label).toFloat()
        paint.strokeWidth = strokeWidth.toFloat()
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    fun setStartLabel(value: String?) {
        startLabel = value ?: ""
    }

    fun setEndLabel(value: String?) {
        endLabel = value ?: ""
    }

    fun setFloatingLabel(value: String?) {
        floatingLabel = value ?: ""
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewRect.apply {
            top = horizon_bottom_margin - textMargin.toInt()
            right = w
            left = 0
            bottom = h
        }
        availableViewWidth = w - circleRadius * 4
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLabels(canvas)
        drawHorizontal(canvas)
        clipCanvas(canvas)
        drawSun(canvas)
    }

    private fun drawLabels(canvas: Canvas) {
        val y = viewRect.bottom - 1f
        drawStartLabel(canvas, y)
        drawEndLabel(canvas, y)
    }

    private fun drawStartLabel(canvas: Canvas, y: Float) {
        val x = viewRect.left + leftMarginLabel
        canvas.drawText(startLabel, x, y, paint)
    }

    private fun drawEndLabel(canvas: Canvas, y: Float) {
        val textWidth = getTextBounds(endLabel).width()
        val x = viewRect.right - textWidth - rightMarginLabel
        canvas.drawText(endLabel, x, y, paint)
    }

    private fun drawHorizontal(canvas: Canvas) {
        val y = viewRect.bottom - horizon_bottom_margin.toFloat()
        val fromX = viewRect.right.toFloat()
        val toX = viewRect.left.toFloat()
        canvas.drawLine(toX, y, fromX, y, paint)
    }

    private fun drawSun(canvas: Canvas) {
        drawCircle(canvas)
        if (sunIsVisible) drawFloatingLabel(canvas)
    }

    private fun clipCanvas(canvas: Canvas) {
        canvas.clipRect(0,0, canvas.width,canvas.height - horizon_bottom_margin)
    }

    fun setPercentProgress(progress: Double) {
        val position = Math.PI + progress * Math.PI
        if (progress > 1 || progress < 0) return clearSun()
        sunCoordinates.x = ((50 + cos(position) * 50) / 100).toFloat()
        sunCoordinates.y = (abs(sin(position) * 100) / 100).toFloat()
        sunIsVisible = true
        invalidate()
    }

    private fun clearSun() {
        sunCoordinates.x = 0f
        sunCoordinates.y = 0f
        sunIsVisible = false
    }

    private fun drawCircle(canvas: Canvas) {
        val sunX = sunCoordinates.x * availableViewWidth
        val sunY = sunCoordinates.y * viewRect.height()
        val cx = sunX + (circleRadius * 2).toFloat()
        val cy = (viewRect.height() - sunY) + circleRadius + textMargin
        val radius = circleRadius.toFloat()
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawFloatingLabel(canvas: Canvas) {
        val bounds = getTextBounds(floatingLabel)
        val sunX = sunCoordinates.x * availableViewWidth
        val sunY = sunCoordinates.y * viewRect.height()
        val halfTextWidth = bounds.width() / 2
        val doubleTextHeight = bounds.height() * 2
        val sunDiameter = circleRadius * 2f
        val x = (sunX - halfTextWidth).toInt() + sunDiameter
        val y = (viewRect.height() - sunY - doubleTextHeight).toInt() + circleRadius + textMargin
        canvas.drawText(floatingLabel, x, y, paint)
    }

    private fun getTextBounds(text: String): Rect {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredWidth * 0.6
        val newHeight = height.toInt()
        setMeasuredDimension(measuredWidth, newHeight)
    }
}