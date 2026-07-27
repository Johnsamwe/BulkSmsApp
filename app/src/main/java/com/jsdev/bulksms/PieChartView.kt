package com.jsdev.bulksms

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Pie/donut chart nyepesi iliyojengwa kwa Canvas ya Android moja kwa moja —
 * hakuna maktaba ya nje (MPAndroidChart n.k.) ili kuepuka hatari ya build
 * kuvunjika kutokana na repository/dependency mpya.
 */
class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Slice(val label: String, val value: Float, val color: Int)

    private var slices: List<Slice> = emptyList()
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1B2420")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8672")
        textAlign = Paint.Align.CENTER
    }
    private val rectF = RectF()
    private var centerLabel: String = ""

    fun setData(data: List<Slice>, centerText: String = "") {
        slices = data.filter { it.value > 0f }
        centerLabel = centerText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        if (size <= 0f) return

        if (slices.isEmpty()) {
            emptyPaint.textSize = size * 0.08f
            canvas.drawText("Hakuna data bado", width / 2f, height / 2f, emptyPaint)
            return
        }

        val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
        val strokeWidth = size * 0.20f
        val pad = strokeWidth / 2f + 4f
        val left = (width - size) / 2f + pad
        val top = (height - size) / 2f + pad
        rectF.set(left, top, left + size - 2 * pad, top + size - 2 * pad)

        arcPaint.strokeWidth = strokeWidth
        arcPaint.strokeCap = Paint.Cap.BUTT

        var startAngle = -90f
        for (s in slices) {
            val sweep = (s.value / total) * 360f
            arcPaint.color = s.color
            canvas.drawArc(rectF, startAngle, sweep.coerceAtLeast(0.5f), false, arcPaint)
            startAngle += sweep
        }

        if (centerLabel.isNotEmpty()) {
            centerTextPaint.textSize = size * 0.13f
            canvas.drawText(centerLabel, width / 2f, height / 2f + size * 0.045f, centerTextPaint)
        }
    }
}
