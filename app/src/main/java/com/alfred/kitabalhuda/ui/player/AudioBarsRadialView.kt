package com.alfred.kitabalhuda.ui.player

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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

    private val density = context.resources.displayMetrics.density

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF03DAC5.toInt()
        style = Paint.Style.FILL
        alpha = 180
    }


    private val barCount = 16
    private val barWidth = 4.5f * density
    private val barMaxHeight = 8f * density
    private val cornerRadius = barWidth / 2f

    private val phases = FloatArray(barCount) { Random.nextFloat() * 360f }
    private val amplitudes = FloatArray(barCount) { 0.4f + Random.nextFloat() * 0.6f }
    private val speeds = FloatArray(barCount) { 2.4f + Random.nextFloat() * 3.6f }

    private var animator: ValueAnimator? = null
    private var startTime = 0L
    private var frozenTime = -1f

    private val outerRadii = floatArrayOf(
        cornerRadius, cornerRadius, cornerRadius, cornerRadius,
        0f, 0f, 0f, 0f
    )
    private val barPath = Path()
    private val barRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val half = minOf(cx, cy)
        if (half <= 0f) return

        val innerRadius = half
        val containerRadius = innerRadius + barMaxHeight
        val angleStep = 360f / barCount
        val time = if (frozenTime >= 0f) frozenTime
            else if (startTime > 0) (System.nanoTime() - startTime) / 1_000_000_000f
            else 0f

        // Clip to circular container
        canvas.save()
        val circlePath = Path().apply {
            addCircle(cx, cy, containerRadius, Path.Direction.CW)
        }
        canvas.clipPath(circlePath)

        // Subtle circle fill
        canvas.drawColor(0x0803DAC5.toInt())

        // Draw bars inside the circle
        canvas.save()
        canvas.translate(cx, cy)

        for (i in 0 until barCount) {
            val rawSin = sin(time * speeds[i] + phases[i]).toFloat()
            val normalized = 0.5f + 0.5f * rawSin.coerceIn(-1f, 1f) * amplitudes[i]
            val height = barMaxHeight * normalized.coerceIn(0f, 1f)

            canvas.save()
            canvas.rotate(angleStep * i)

            val halfW = barWidth / 2f
            barRect.set(-halfW, -innerRadius - height, halfW, -innerRadius)
            barPath.rewind()
            barPath.addRoundRect(barRect, outerRadii, Path.Direction.CW)
            canvas.drawPath(barPath, barPaint)
            canvas.restore()
        }

        canvas.restore()
        canvas.restore()
    }

    fun startAnim() {
        if (visibility != VISIBLE) visibility = VISIBLE
        if (animator?.isRunning == true) return
        frozenTime = -1f
        startTime = System.nanoTime()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            duration = 2500
            addUpdateListener { invalidate() }
            start()
        }
    }

    fun pauseAnim() {
        if (startTime > 0) {
            frozenTime = (System.nanoTime() - startTime) / 1_000_000_000f
        }
        animator?.cancel()
        animator = null
        invalidate()
    }

    fun stopAnim() {
        visibility = GONE
        frozenTime = -1f
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
