package com.example.photoedit.iu.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.adapter.FilterSelectionAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityHomeEditImageBinding
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.fixBitmapOrientation
import com.example.photoedit.utils.saveImageToGallery
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import java.io.File

class HomeEditImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeEditImageBinding
    private var imagePath: String? = null
    private var subscription: CompositeDisposable = CompositeDisposable()
    private val filterNames = listOf(
        "Original",
        "Sepia",
        "Contrast",
        "GrayScale",
        "GaussianBlur",
        "Brightness",
        "Saturation",
        "Invert",
        "Vignette"
    )
    private var bitmap: Bitmap? = null
    private lateinit var gpuImage: GPUImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gpuImage = GPUImage(this)

        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)

        Glide.with(this)
            .load(imagePath)
            .into(binding.imgViewImage)

        binding.btnBack.setOnClickListener {
            dialogFinished(getString(R.string.cancel_dialog), this, imagePath) { finish() }
        }

        onBackPressedDispatcher.addCallback {
            binding.btnBack.performClick()
        }
        binding.btnShare.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            shareImage()

        }

        binding.btnFilter.setOnClickListener {
            binding.recyclerFilter.visibility = View.VISIBLE
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.beautiful)

            val adapter = FilterSelectionAdapter(originalBitmap, filterNames) { filter ->
                applyFilter(filter)
            }
            binding.recyclerFilter.adapter = adapter


        }

        binding.btnSave.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            val saveBitmap = if (bitmap == null) {
                val mainBitmap = BitmapFactory.decodeFile(imagePath)
                val rotatedBitmap = imagePath?.let { fixBitmapOrientation(mainBitmap, it) }

                val newBitmap = rotatedBitmap?.let {
                    Bitmap.createBitmap(
                        it.width,
                        rotatedBitmap.height,
                        rotatedBitmap.config
                    )
                }
                val canvas = newBitmap?.let { Canvas(it) }
                rotatedBitmap?.let {
                    canvas?.drawBitmap(it, 0f, 0f, null)
                    rotatedBitmap
                }
            } else {
                bitmap
            }

            subscription.add(
                saveImageToGallery(this, saveBitmap!!).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this, "save image $it", Toast.LENGTH_SHORT).show()

                    }, {
                        Toast.makeText(this, "don't save image $it", Toast.LENGTH_SHORT).show()

                    })
            )


        }

        binding.btnEdit.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE
            val intent =
                Intent(this, EditPhotoActivity::class.java).apply {
                    putExtra(Constants.KEY_IMAGE_PATH, imagePath)

                }
            startActivity(intent)
        }

    }

    private fun shareImage() {
        imagePath?.let { path ->
            val file = File(path)
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.photoedit.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share image via"))
        }
    }


    private fun applyFilter(filterName: String) {
        val filter = when (filterName) {
            "Original" -> null
            "Sepia" -> GPUImageSepiaToneFilter()
            "Contrast" -> GPUImageContrastFilter(2.0f)
            "GrayScale" -> GPUImageGrayscaleFilter()
            "GaussianBlur" -> GPUImageGaussianBlurFilter()
            "Brightness" -> GPUImageBrightnessFilter(0.5f)
            "Saturation" -> GPUImageSaturationFilter(2.0f)
            "Invert" -> GPUImageColorInvertFilter()
            "Vignette" -> GPUImageVignetteFilter()
            else -> null
        }
        val mainBitmap = BitmapFactory.decodeFile(imagePath)
        val rotatedBitmap = imagePath?.let { fixBitmapOrientation(mainBitmap, it) }

        val newBitmap = rotatedBitmap?.let {
            Bitmap.createBitmap(
                it.width,
                rotatedBitmap.height,
                rotatedBitmap.config
            )
        }

        val canvas = newBitmap?.let { Canvas(it) }
        rotatedBitmap?.let { canvas?.drawBitmap(it, 0f, 0f, null) }

        gpuImage.setImage(rotatedBitmap)

        if (filter != null) {
            gpuImage.setFilter(filter)
            val filteredBitmap = gpuImage.bitmapWithFilterApplied
            bitmap = filteredBitmap
            binding.imgViewImage.setImageBitmap(filteredBitmap)
        } else {
            bitmap = null
            binding.imgViewImage.setImageBitmap(newBitmap)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.clear()
    }
}
