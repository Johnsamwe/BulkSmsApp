package com.jsdev.bulksms

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Bar(val label: String, val value: Float, val color: Int)

    private var bars: List<Bar> = emptyList()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5A5748")
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1B2420")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8672")
        textAlign = Paint.Align.CENTER
    }

    fun setData(data: List<Bar>) {
        bars = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) {
            emptyPaint.textSize = height * 0.12f
            canvas.drawText("Hakuna data bado", width / 2f, height / 2f, emptyPaint)
            return
        }

        val labelSpace = height * 0.14f
        val valueSpace = height * 0.12f
        val chartBottom = height - labelSpace
        val chartTop = valueSpace
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val maxVal = (bars.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
        val n = bars.size
        val slot = width.toFloat() / n
        val barWidth = slot * 0.5f

        labelPaint.textSize = labelSpace * 0.55f
        valuePaint.textSize = valueSpace * 0.65f

        bars.forEachIndexed { i, bar ->
            val centerX = slot * i + slot / 2f
            val barH = (bar.value / maxVal) * chartHeight
            val top = chartBottom - barH
            barPaint.color = bar.color
            canvas.drawRoundRect(
                centerX - barWidth / 2f, top, centerX + barWidth / 2f, chartBottom,
                10f, 10f, barPaint
            )
            canvas.drawText(bar.value.toInt().toString(), centerX, (top - 8f).coerceAtLeast(valueSpace * 0.8f), valuePaint)
            canvas.drawText(bar.label, centerX, height - labelSpace * 0.25f, labelPaint)
        }
    }
}
