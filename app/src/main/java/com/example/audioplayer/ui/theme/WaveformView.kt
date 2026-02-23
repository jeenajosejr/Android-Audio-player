package com.example.audioplayer.ui.theme

import android.content.Context
import android.graphics.*
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var visualizer: Visualizer? = null
    private var waveformData: ByteArray? = null

    private val paint = Paint().apply {
        color = Color.parseColor("#B388FF")   //  Soft purple
        strokeWidth = 2f                      // Thin lines
        strokeCap = Paint.Cap.ROUND           // Rounded edges
        isAntiAlias = true
    }
    fun attachToSession(audioSessionId: Int) {

        release()

        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object :
                    Visualizer.OnDataCaptureListener {

                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        bytes: ByteArray?,
                        samplingRate: Int
                    ) {
                        waveformData = bytes
                        invalidate()
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        bytes: ByteArray?,
                        samplingRate: Int
                    ) {
                    }
                },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = waveformData ?: return drawPlaceholder(canvas)

        val centerY = height / 2f
        val widthF = width.toFloat()

        val xStep = widthF / data.size
        var x = 0f

        for (i in data.indices step 3) {   // Skip more values = thinner waves

            val value = data[i].toInt()

            // 🔥 THIS controls wave height
            val scaledHeight = (value / 128f) * (height / 14f)
            // ↑ increase 10f → 12f or 14f for smaller waves

            val top = centerY - scaledHeight
            val bottom = centerY + scaledHeight

            canvas.drawLine(x, top, x, bottom, paint)

            x += xStep
        }
    }

    private fun drawPlaceholder(canvas: Canvas) {

        val barWidth = width / 20f
        val gap = barWidth / 2
        var x = 0f

        for (i in 0..20) {
            val randomHeight = (Math.random() * height / 2).toFloat()
            canvas.drawRect(
                x,
                height / 2 - randomHeight,
                x + barWidth,
                height / 2 + randomHeight,
                paint
            )
            x += barWidth + gap
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }
}
