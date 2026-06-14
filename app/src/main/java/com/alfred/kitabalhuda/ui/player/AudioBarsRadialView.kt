package com.alfred.kitabalhuda.ui.player

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin
import kotlin.random.Random

class AudioBarsRadialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var innerRadius = 0f
    private val density = context.resources.displayMetrics.density

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF03DAC5.toInt()
        style = Paint.Style.FILL
        alpha = 180
    }

    private val barCount = 16
    private val barWidth = 3f * density
    private val barMaxHeight = 10f * density
    private val cornerRadius = barWidth / 2f

    private val phases = FloatArray(barCount) { Random.nextFloat() * 360f }
    private val amplitudes = FloatArray(barCount) { 0.4f + Random.nextFloat() * 0.6f }
    private val speeds = FloatArray(barCount) { 0.8f + Random.nextFloat() * 1.4f }

    private var animator: ValueAnimator? = null
    private var startTime = 0L

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        innerRadius = minOf(w, h) / 2f - barMaxHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (innerRadius <= 0f) return

        val cx = width / 2f
        val cy = height / 2f
        val angleStep = 360f / barCount
        val time = if (startTime > 0) (System.nanoTime() - startTime) / 1_000_000_000f else 0f

        canvas.save()
        canvas.translate(cx, cy)

        for (i in 0 until barCount) {
            val rawSin = sin(time * speeds[i] + phases[i]).toFloat()
            val normalized = 0.5f + 0.5f * rawSin.coerceIn(-1f, 1f) * amplitudes[i]
            val height = barMaxHeight * normalized.coerceIn(0f, 1f)

            canvas.save()
            canvas.rotate(angleStep * i)

            val halfW = barWidth / 2f
            canvas.drawRoundRect(
                -halfW, -innerRadius - height,
                halfW, -innerRadius,
                cornerRadius, cornerRadius,
                barPaint
            )
            canvas.restore()
        }

        canvas.restore()
    }

    fun startAnim() {
        if (visibility != VISIBLE) visibility = VISIBLE
        if (animator?.isRunning == true) return
        startTime = System.nanoTime()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            duration = 2500
            addUpdateListener { invalidate() }
            start()
        }
    }

    fun stopAnim() {
        visibility = GONE
        animator?.cancel()
        animator = null
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }
}
