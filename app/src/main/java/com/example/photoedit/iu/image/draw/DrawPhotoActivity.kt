package com.example.photoedit.iu.image.draw

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photoedit.R
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityDrawPhotoBinding
import com.example.photoedit.databinding.DialogBorderBinding
import com.example.photoedit.iu.view.DrawCustom
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.fixBitmapOrientation
import com.example.photoedit.utils.getContentBounds
import com.example.photoedit.utils.saveImage
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.bottomsheet.BottomSheetDialog

class DrawPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDrawPhotoBinding
    private lateinit var paintview: DrawCustom
    private var imagePath: String? = null
    private var backgroundImage: String? = null
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent?.getStringExtra(Constants.KEY_IMAGE_PATH)
        backgroundImage = intent?.getStringExtra(Constants.KEY_IMAGE_BACKGROUND)
        paintview = binding.paintView
        binding.seekStroke.progress = 10

        binding.seekStroke.max = 40
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.seekStroke.min = 5
        }


        Glide.with(this)
            .load(imagePath)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

            })
            .into(binding.imageView)



        val mainBitmap = BitmapFactory.decodeFile(imagePath)
        bitmap = imagePath?.let { fixBitmapOrientation(mainBitmap, it) }
        bitmap?.let { paintview.setBitmap(it) }


        binding.viewColor.setOnClickListener {
            ColorPickerDialog
                .Builder(this)
                .setTitle("Pick Theme")
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(R.color.white)
                .setColorListener { color, _ ->
                    binding.viewColor.backgroundTintList = ColorStateList.valueOf(color)
                    paintview.setColorDraw(color)

                }
                .show()
        }

        binding.btnLine.setOnClickListener {
            paintview.setEraserMode(false)
            paintview.setLine()

        }

        binding.btnDashed.setOnClickListener {
            paintview.setEraserMode(false)
            paintview.setDashed()
        }

        binding.btnEraser.setOnClickListener {
            paintview.setEraserMode(true)

        }

        binding.seekStroke.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    paintview.setSize(progress.toFloat())
                }

                override fun onStartTrackingTouch(seek: SeekBar) {}
                override fun onStopTrackingTouch(seek: SeekBar) {}
            }
        )

        binding.btnReset.setOnClickListener {
            paintview.setReset()
        }

        binding.btnBorder.setOnClickListener {
            dialogBorder()
        }


        binding.btnBack.setOnClickListener {
            dialogFinished(getString(R.string.cancel_changes_dialog), this, null) { finish() }
        }

        onBackPressedDispatcher.addCallback {
            binding.btnBack.performClick()
        }

        binding.btnSave.setOnClickListener {
            val frameLayout = binding.frameContainer
            val originalBitmap = Bitmap.createBitmap(frameLayout.width, frameLayout.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(originalBitmap)
            binding.frameContainer.draw(canvas)


            val contentBounds = getContentBounds(originalBitmap)

            val croppedBitmap = Bitmap.createBitmap(
                originalBitmap,
                contentBounds.left,
                contentBounds.top,
                contentBounds.width(),
                contentBounds.height()
            )

            imagePath = imagePath?.let { path ->
                saveImage(this, croppedBitmap, path)
            }
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.KEY_IMAGE_PATH, imagePath)
            returnIntent.putExtra(Constants.KEY_IMAGE_BACKGROUND, backgroundImage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }


    }





    private fun dialogBorder() {
        val bindingDialog = DialogBorderBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.apply {
            seekBar.progress = paintview.getBorderStroke()
            seekBar.max = 60
            btnColor.setOnClickListener {
                ColorPickerDialog
                    .Builder(this@DrawPhotoActivity)
                    .setTitle("Pick Theme")
                    .setColorShape(ColorShape.SQAURE)
                    .setDefaultColor(R.color.black)
                    .setColorListener { color, _ ->
                        viewColor.backgroundTintList = ColorStateList.valueOf(color)
                        paintview.setBorderColor(color)
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
                        paintview.setBorderStroke(progress)
                        val maxPadding = minOf(binding.imageView.width, binding.imageView.height) / 2
                        val padding = progress.coerceAtMost(maxPadding) / 2
                        binding.imageView.setPadding(padding, padding, padding, padding)
                    }

                    override fun onStartTrackingTouch(seek: SeekBar) {}
                    override fun onStopTrackingTouch(seek: SeekBar) {}
                }
            )
            btnBack.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()
    }
}
