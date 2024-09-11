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
import kotlin.time.Duration

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>()
    private val recList = mutableListOf<RectF>()

    private val rectWidth = 10f
    private var tick = 0

    private val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for(recF in recList){
            canvas?.drawRect(recF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {

        ampList.add(maxAmplitude)
        recList.clear()


        val maxRect = (this.width / rectWidth).toInt()

        val amps = ampList.takeLast(maxRect)

        for ((i,amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = 0f
            rectF.bottom = amp
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth

            recList.add(rectF)
        }

        invalidate()

    }

    fun replayAmplitude(duration: Int) {
        recList.clear()


        val maxRectF = (this.width / rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxRectF)

        for ((i,amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = 0f
            rectF.bottom = amp
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth

            recList.add(rectF)
        }

        tick++

        invalidate()
    }

    fun clearData() {
        ampList.clear()
    }

    fun clearWave() {
        recList.clear()
        tick = 0
        invalidate()
    }
}