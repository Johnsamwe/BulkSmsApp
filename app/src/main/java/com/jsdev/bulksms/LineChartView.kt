package com.jsdev.bulksms

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Line chart nyepesi inayoonyesha mwenendo wa kiwango cha mafanikio (%)
 * kwa kampeni za hivi karibuni, kutoka ya zamani hadi ya karibuni.
 */
class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Float> = emptyList() // asilimia 0-100

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F6F4E")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E7F3EC")
        style = Paint.Style.FILL
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1F6F4E") }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D8D3C4")
        strokeWidth = 2f
    }
    private val gridLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8672")
        textAlign = Paint.Align.LEFT
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8672")
        textAlign = Paint.Align.CENTER
    }

    fun setData(data: List<Float>) {
        values = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (values.isEmpty()) {
            emptyPaint.textSize = height * 0.12f
            canvas.drawText("Hakuna kampeni bado", width / 2f, height / 2f, emptyPaint)
            return
        }

        val leftPad = width * 0.09f
        val rightPad = width * 0.03f
        val topPad = height * 0.10f
        val bottomPad = height * 0.10f
        val chartW = width - leftPad - rightPad
        val chartH = height - topPad - bottomPad

        gridLabelPaint.textSize = height * 0.07f

        // Gridi za mlalo kwa 0%, 50%, 100%
        listOf(0f, 50f, 100f).forEach { pct ->
            val y = topPad + chartH - (pct / 100f) * chartH
            canvas.drawLine(leftPad, y, width - rightPad, y, gridPaint)
            canvas.drawText("${pct.toInt()}%", 2f, y - 4f, gridLabelPaint)
        }

        if (values.size == 1) {
            val x = leftPad + chartW / 2f
            val y = topPad + chartH - (values[0] / 100f) * chartH
            canvas.drawCircle(x, y, 8f, dotPaint)
            return
        }

        val stepX = chartW / (values.size - 1)
        val path = android.graphics.Path()
        val fillPath = android.graphics.Path()

        values.forEachIndexed { i, v ->
            val x = leftPad + stepX * i
            val y = topPad + chartH - (v / 100f) * chartH
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, topPad + chartH)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(leftPad + stepX * (values.size - 1), topPad + chartH)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)

        values.forEachIndexed { i, v ->
            val x = leftPad + stepX * i
            val y = topPad + chartH - (v / 100f) * chartH
            canvas.drawCircle(x, y, 6f, dotPaint)
        }
    }
}
