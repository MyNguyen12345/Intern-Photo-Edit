package com.example.photoedit.iu.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.example.photoedit.R
import com.example.photoedit.databinding.DialogTextCustomBinding
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.bottomsheet.BottomSheetDialog

class EditTextView(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    private var isDragging = false
    private var lastX = 0f
    private var lastY = 0f
    private var isMoving = false
    private var isBackground = false

    init {
        setBackgroundColor(Color.TRANSPARENT)
        setTextColor(Color.WHITE)
        textSize = 18f

        isFocusable = true
        isFocusableInTouchMode = true
        isCursorVisible = true
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("TAG", "onTouchEvent: doewn")
                if (!hasFocus()) {
                    isDragging = true
                    lastX = event.rawX
                    lastY = event.rawY
                    Log.d("TAG", "onTouchEvent: dkkdkd")
                } else {
                    Log.d("TAG", "onTouchEvent: sssss")
                    isDragging = false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = event.rawX - lastX
                    val deltaY = event.rawY - lastY

                    x += deltaX
                    y += deltaY

                    lastX = event.rawX
                    lastY = event.rawY
                    isMoving = true
                }
            }

            MotionEvent.ACTION_UP -> {
                isDragging = false

                if (!isMoving) {
                    requestFocus()
                    Log.d("TAG", "onTouchEvent: kkkks")
                    dialogCustomEdit()
                }
                isMoving = false
            }
        }

        return if (isDragging) true else super.onTouchEvent(event)
    }

    private fun dialogCustomEdit() {
        val builder = AlertDialog.Builder(context)
        val bindingDialog = DialogTextCustomBinding.inflate(LayoutInflater.from(context))

        builder.setView(bindingDialog.root)
        val dialog = builder.create()
        var color1 = Color.WHITE
        var fontStyleState = 0

        bindingDialog.apply {
            seekBar.progress = textSize.toInt()
            seekBar.max = 100
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                seekBar.min = 10
            }

            btnStyle.setOnClickListener {
                when (fontStyleState) {
                    0 -> {
                        typeface = Typeface.create("serif", Typeface.BOLD)
                        fontStyleState = 1
                    }

                    1 -> {
                        typeface = Typeface.create("sans-serif", Typeface.ITALIC)
                        fontStyleState = 2
                    }

                    2 -> {
                        typeface = Typeface.create("monospace", Typeface.NORMAL)
                        fontStyleState = 0
                    }
                }

            }
            btnBackground.setOnClickListener {
                isBackground = !isBackground
                if (isBackground) {
                    setTextColor(color1)
                    setBackgroundColor(Color.TRANSPARENT)
                } else {
                    setTextColor(Color.WHITE)

                    setBackgroundColor(color1)

                }
            }
            viewColor.setOnClickListener {
                ColorPickerDialog
                    .Builder(context)
                    .setTitle("Pick Theme")
                    .setColorShape(ColorShape.SQAURE)
                    .setDefaultColor(R.color.black)
                    .setColorListener { color, _ ->
                        viewColor.backgroundTintList = ColorStateList.valueOf(color)
                        color1 = color
                        if (isBackground) {
                            setTextColor(color)
                            setBackgroundColor(Color.TRANSPARENT)
                        } else {
                            setTextColor(Color.WHITE)

                            setBackgroundColor(color)

                        }

                    }
                    .show()
            }

            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seek: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textSize = progress.toFloat()
                    }

                    override fun onStartTrackingTouch(seek: SeekBar) {}
                    override fun onStopTrackingTouch(seek: SeekBar) {}
                }
            )
            btnBack.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()
        val window = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val params = window?.attributes
        params?.gravity = android.view.Gravity.BOTTOM
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
    }

    override fun clearFocus() {
        super.clearFocus()
        Log.d("TAG", "clearFocus: ldldld")
        isCursorVisible = false
    }

}
