package com.example.photoedit.iu.image.color

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityColorImageBinding
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.fixBitmapOrientation
import com.example.photoedit.utils.saveImage
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter

class ColorImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityColorImageBinding
    private var imagePath: String? = null
    private lateinit var gpuImage: GPUImage
    private var bitmapSave: Bitmap? = null
    private var backgroundImage: String? = null
    private var bitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)
        backgroundImage = intent?.getStringExtra(Constants.KEY_IMAGE_BACKGROUND)


        Glide.with(this)
            .load(imagePath)
            .into(binding.viewImage)

        binding.btnBack.setOnClickListener {
            dialogFinished(getString(R.string.cancel_changes_dialog), this, null) { finish() }
        }

        onBackPressedDispatcher.addCallback {
            binding.btnBack.performClick()
        }
        binding.btnSave.setOnClickListener {
            imagePath =
                bitmapSave?.let { it1 -> imagePath?.let { it2 -> saveImage(this, it1, it2) } }
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.KEY_IMAGE_PATH, imagePath)
            returnIntent.putExtra(Constants.KEY_IMAGE_BACKGROUND, backgroundImage)

            setResult(Activity.RESULT_OK, returnIntent)
            finish()


        }

        binding.seekBarBrightness.progress = 100
        binding.seekBarBlur.progress = 0
        binding.seekBarColor.max = 360
        binding.seekBarBrightness.max = 100
        binding.seekBarBlur.max = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.seekBarColor.min = 0
            binding.seekBarBrightness.min = 0
            binding.seekBarBlur.min = 0
        }
        val originalBitmap = BitmapFactory.decodeFile(imagePath)
        bitmap = imagePath?.let { fixBitmapOrientation(originalBitmap, it) }
        gpuImage = GPUImage(this)
        gpuImage.setImage(bitmap)

        binding.seekBarColor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                applyFilters()

            }
        })

        binding.seekBarBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                applyFilters()

            }

        })

        binding.seekBarBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                applyFilters()
            }

        })
    }

    private fun applyFilters() {
        val brightness = binding.seekBarBrightness.progress / 100.0f
        val hue = binding.seekBarColor.progress.toFloat()
        val blurValue = binding.seekBarBlur.progress / 100.0f * 25

        var modifiedBitmap = bitmap?.let { brightness(it, brightness) }

        if (blurValue > 0) {
            modifiedBitmap = modifiedBitmap?.let { blur(it, blurValue) }
        }

        gpuImage.setImage(modifiedBitmap)
        val hueFilter = GPUImageHueFilter(hue)
        gpuImage.setFilter(hueFilter)

        bitmapSave = gpuImage.bitmapWithFilterApplied
        binding.viewImage.setImageBitmap(gpuImage.bitmapWithFilterApplied)
    }


    private fun brightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(newBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(brightness, brightness, brightness, 1.0f)

        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return newBitmap
    }

    private fun blur(image: Bitmap, values: Float): Bitmap {
        if (values <= 0) return image
        val width = Math.round(image.width.toFloat())
        val height = Math.round(image.height.toFloat())

        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val rs = RenderScript.create(this)

        val intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)

        intrinsicBlur.setRadius(values)
        intrinsicBlur.setInput(tmpIn)
        intrinsicBlur.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        return outputBitmap
    }
}
