package com.example.photoedit.iu.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class CustomImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var isDragging = false
    private var lastX = 0f
    private var lastY = 0f
    private var isMoving = false
    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private val imageMatrix = Matrix()

    init {
        setBackgroundColor(Color.TRANSPARENT)
        isFocusable = true
        isFocusableInTouchMode = true
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        scaleType = ScaleType.MATRIX
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                lastX = event.rawX
                lastY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY
//                    imageMatrix.postTranslate(deltaX, deltaY)

                    x = (x + deltaX)
                    y = (y + deltaY)


                    lastX = event.rawX
                    lastY = event.rawY
                    isMoving = true
                }
            }

            MotionEvent.ACTION_UP -> {
                isDragging = false

                if (!isMoving) {
                    requestFocus()
                }
                isMoving = false
            }
        }
        setImageMatrix(imageMatrix)
        return if (isDragging) true else super.onTouchEvent(event)
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor

            scaleFactor = scaleFactor.coerceIn(0.2f, 1.1f)
            imageMatrix.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)

            return true
        }
    }

}
