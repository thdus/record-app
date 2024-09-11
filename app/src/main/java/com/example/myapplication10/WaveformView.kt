package com.example.myapplication10

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.app.NotificationCompat.Style
import org.w3c.dom.Attr

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    val recF = RectF(20f, 30f, 20f + 30f, 30f + 60f)
    val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(recF, redPaint)
    }

    fun addAmplitude(maxAmplitude: Float) {
        recF.top = 0f
        recF.bottom = maxAmplitude
        recF.left = 0f
        recF.right = recF.left + 20f

        invalidate()
    }
}