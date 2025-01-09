package com.example.photoedit.iu.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class FocusCircleView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint = Paint()

    var focusCircle: RectF? = null

    private var handler = Handler(Looper.getMainLooper())
    private var removeFocusRunnable = Runnable { }

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        focusCircle?.let { rect ->
            val outerRadius = rect.width() / 1.5f
            val innerRadius = outerRadius / 2
            canvas.drawCircle(rect.centerX(), rect.centerY(), outerRadius, paint)
            canvas.drawCircle(rect.centerX(), rect.centerY(), innerRadius, paint)
            scheduleFocusCircleRemoval()
        }
    }

    private fun scheduleFocusCircleRemoval() {
        handler.removeCallbacks(removeFocusRunnable)
        removeFocusRunnable = Runnable {
            focusCircle = null
            invalidate()
        }
        handler.postDelayed(removeFocusRunnable, 2000)
    }
}