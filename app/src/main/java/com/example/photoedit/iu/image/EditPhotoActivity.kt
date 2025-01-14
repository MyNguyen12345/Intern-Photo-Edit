package com.example.photoedit.iu.image

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityEditPhotoBinding
import com.example.photoedit.iu.view.EditTextView
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class EditPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPhotoBinding
    private var imagePath: String? = null

    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>
    private val editTextViews: MutableList<EditTextView> = mutableListOf()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)


        Glide.with(this)
            .load(imagePath)
            .into(binding.imgViewImage)
        binding.btnCrop.setOnClickListener {
            startImageCrop()

        }
        binding.btnSticker.setOnClickListener {
            Log.d("TAG", "onCreate: " + imagePath)
        }

        binding.btnText.setOnClickListener {
            val newEditTextView = EditTextView(this, null).apply {
                id = View.generateViewId()
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {

                    topToTop = binding.containerLayout.id
                    startToStart = binding.containerLayout.id
                    bottomToBottom = binding.containerLayout.id
                    endToEnd = binding.containerLayout.id
                }

                hint = "Nhập văn bản"
                setHintTextColor(Color.WHITE)
                setTextColor(Color.WHITE)
                textSize = 18f
                setBackgroundColor(Color.TRANSPARENT)
            }

            binding.containerLayout.addView(newEditTextView)
            editTextViews.add(newEditTextView)


        }

        binding.btnSave.setOnClickListener {
            addTextToImage()
        }

        cropImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val resultUri = UCrop.getOutput(data!!)
                    resultUri?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.imgViewImage)

                    }
                    imagePath?.let { deleteImageFromInternalStorage(it) }
                    imagePath = resultUri.toString()


                } else if (result.resultCode == UCrop.RESULT_ERROR) {
                    val cropError = UCrop.getError(result.data!!)
                    Log.e("TAG", "Error cropping image: ${cropError?.message}")
                }
            }

    }


    private fun deleteImageFromInternalStorage(imagePath: String): Boolean {
        val file = File(imagePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    private fun startImageCrop() {
        imagePath?.let { path ->
            val sourceFile = File(path)
            val sourceUri = FileProvider.getUriForFile(
                this,
                "com.example.photoedit.provider",
                sourceFile
            )


            val imageFolder = File(cacheDir, "Images")
            if (!imageFolder.exists()) {
                imageFolder.mkdir()
            }

            val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"

            val imageFile = File(imageFolder, fileName)

            val destinationUri = Uri.fromFile(imageFile)


            val uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(1920, 1080)
                .getIntent(this)

            cropImageLauncher.launch(uCropIntent)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus

        if (view is EditTextView) {
            val outRect = Rect()
            view.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                view.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }


    private fun addTextToImage() {
        if (imagePath == null) return
        val containerLayout = binding.container

        val bitmap = Bitmap.createBitmap(
            containerLayout.width,
            containerLayout.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        containerLayout.draw(canvas)

        saveBitmapToFile(bitmap)

    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "images"
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)

        try {
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(this, "Image saved at: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}