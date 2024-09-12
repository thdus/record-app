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

    private val rectWidth = 15f
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
        val height = this.height
        val amplitude = (maxAmplitude / Short.MAX_VALUE) * this.height * 0.8f

        ampList.add(amplitude)
        recList.clear()


        val maxRect = (this.width / rectWidth).toInt()

        val amps = ampList.takeLast(maxRect)

        for ((i,amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 -3f
            rectF.bottom = amp
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 5f // 여백을 위해 5를 더 줌

            recList.add(rectF)
        }

        invalidate()

    }

    fun replayAmplitude() {
        recList.clear()


        val maxRectF = (this.width / rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxRectF)

        for ((i,amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 -2f
            rectF.bottom = rectF.top + amp + 3f
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 5f

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