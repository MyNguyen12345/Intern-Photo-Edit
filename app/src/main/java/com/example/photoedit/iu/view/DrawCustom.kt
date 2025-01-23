package com.example.photoedit.iu.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class DrawCustom @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var touchX: Float = -1f
    private var touchY: Float = -1f
    private var drawSize = 10f
    private var isEraserMode = false
    private var colorDraw = Color.WHITE
    private var dashPathEffect = DashPathEffect(floatArrayOf(30f, 0f), 0f)

    private lateinit var originalBitmap: Bitmap
    private lateinit var canvasBitmap: Bitmap
    private lateinit var canvas: Canvas
    private var drawPath = Path()
    private var lastX = 0f
    private var lastY = 0f
    private var lastXBitmap = 0f
    private var lastYBitmap = 0f
    private var isLine = true


    private var innerRect: RectF? = null
    private var borderRect: RectF? = null

    private var isDrawBitmapMode = false
    private var drawableBitmap: Bitmap? = null


    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 0f
    }


    private val drawPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        color = colorDraw
        style = Paint.Style.STROKE
        strokeWidth = drawSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = null
        alpha = 0xff
        pathEffect = dashPathEffect
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }

    private val imagePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        alpha = 0xff
        pathEffect = DashPathEffect(floatArrayOf(30f, 30f), 0f)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }


    private val eraserPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = drawSize * 3
    }

    fun setBitmap(bitmap: Bitmap) {
        originalBitmap = bitmap

        canvasBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::originalBitmap.isInitialized) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvas = Canvas(canvasBitmap)
            updateRectF()
        }
    }


    fun setColorDraw(color: Int) {
        drawPaint.color = color
        invalidate()
    }

    fun setSize(size: Float) {
        drawPaint.strokeWidth = size
        eraserPaint.strokeWidth = size
        drawSize = size
        if (isLine) {
            drawPaint.pathEffect = dashPathEffect

        } else {
            drawPaint.pathEffect = DashPathEffect(floatArrayOf(drawSize * 2, drawSize * 2), 0f)

        }
        invalidate()
    }

    fun setLine() {
        isLine = true
        drawPaint.pathEffect = dashPathEffect
        invalidate()
    }

    fun setDashed() {
        isLine = false
        drawPaint.pathEffect = DashPathEffect(floatArrayOf(drawSize * 2, drawSize * 2), 0f)
        invalidate()
    }
    fun setDrawableBitmap(bitmap: Bitmap) {
        drawableBitmap = bitmap
        invalidate()
    }

    fun setReset() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun setBorderStroke(stroke: Int) {
        borderPaint.strokeWidth = stroke.toFloat()
        invalidate()
    }

    fun getBorderStroke(): Int = borderPaint.strokeWidth.toInt()

    fun setBorderColor(color: Int) {
        borderPaint.color = color
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (::canvasBitmap.isInitialized) {
            canvas.drawBitmap(canvasBitmap, 0f, 0f, null)
        }

        if (borderPaint.strokeWidth > 0) {
            borderRect?.let { canvas.drawRect(it, borderPaint) }

        }


        val eraser = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL

        }
        if (isEraserMode) {

            canvas.drawRect(
                lastX - drawSize,
                lastY - drawSize,
                lastX + drawSize,
                lastY + drawSize,
                eraser
            )

        }
    }



    private fun updateRectF() {
        val halfBorderWidth = borderPaint.strokeWidth / 2
        val scale =
            min(width.toFloat() / originalBitmap.width, height.toFloat() / originalBitmap.height)

        val scaledWidth = originalBitmap.width * scale
        val scaledHeight = originalBitmap.height * scale
        val left = (width - scaledWidth) / 2
        val top = (height - scaledHeight) / 2
        val right = left + scaledWidth
        val bottom = top + scaledHeight

        borderRect = RectF(left, top, right, bottom)
        innerRect = RectF(
            left + halfBorderWidth,
            top + halfBorderWidth,
            right - halfBorderWidth,
            bottom - halfBorderWidth
        )
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::canvasBitmap.isInitialized) return false
        if (innerRect?.contains(event.x, event.y) == false) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                lastX = touchX
                lastY = touchY
                lastXBitmap = touchX
                lastYBitmap = touchY
                drawPath.reset()
                drawPath.moveTo(touchX, touchY)
            }

            MotionEvent.ACTION_MOVE -> {
                touchX = event.x
                touchY = event.y
                lastX = touchX
                lastY = touchY

                val canvas = Canvas(canvasBitmap)
                if (isEraserMode) {
                    val left = touchX - drawSize
                    val top = touchY - drawSize
                    val right = touchX + drawSize
                    val bottom = touchY + drawSize
                    canvas.drawRect(left, top, right, bottom, eraserPaint)
                } else {

                    if (isDrawBitmapMode) {
                        val distance = hypot(
                            (touchX - lastXBitmap).toDouble(),
                            (touchY - lastYBitmap).toDouble()
                        )
                        if (distance > 10) {
                            drawableBitmap?.let { bitmap ->
                                canvas.drawBitmap(
                                    bitmap,
                                    touchX - bitmap.width / 2,
                                    touchY - bitmap.height / 2,
                                    imagePaint
                                )
                            }
                            lastXBitmap = touchX
                            lastYBitmap = touchY
                        } else {
                            lastXBitmap = touchX
                            lastYBitmap = touchY
                        }
                    } else {
                        lastX = touchX
                        lastY = touchY
                        lastXBitmap = touchX
                        lastYBitmap = touchY
                        drawPath.lineTo(touchX, touchY)
                        canvas.drawPath(drawPath, drawPaint)

                    }
                }

            }

            MotionEvent.ACTION_UP -> {
                if (isEraserMode) {
                    drawPath.reset()
                } else {
                    val canvas = Canvas(canvasBitmap)
                    canvas.drawPath(drawPath, drawPaint)

                }
            }
        }

        invalidate()
        return true
    }

    fun enableBitmapDrawing(enabled: Boolean) {
        isDrawBitmapMode = enabled
    }

    fun setEraserMode(enabled: Boolean) {
        isEraserMode = enabled
    }
}
