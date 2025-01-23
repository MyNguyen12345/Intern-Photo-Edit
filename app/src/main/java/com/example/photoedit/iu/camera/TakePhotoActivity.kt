package com.example.photoedit.iu.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.adapter.FilterSelectionAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityTakePhotoBinding
import com.example.photoedit.iu.album.AlbumActivity
import com.example.photoedit.iu.album.AlbumViewModel
import com.example.photoedit.iu.image.HomeEditImageActivity
import com.example.photoedit.utils.SharedPreferencesUtils
import com.example.photoedit.utils.deleteImageFromInternalStorage
import com.example.photoedit.utils.intentActivity
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TakePhotoActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var orientationEventListener: OrientationEventListener? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9
    private lateinit var imagePathSave: String


    private lateinit var binding: ActivityTakePhotoBinding
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

    private var filter: GPUImageFilter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferencesUtils = SharedPreferencesUtils(this)

        if (sharedPreferencesUtils.isBoolean(Constants.KEY_CHECK_PERMISSION)) {
            startCamera()


        } else {
            Toast.makeText(this, "Check permission ", Toast.LENGTH_SHORT).show()
        }


        binding.imgFlip.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }



        binding.imgPhoto.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            takePhoto()

        }

        binding.imgFlash.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            setFlashIcon(camera)

        }
        binding.cardImage.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            intentActivity(this, AlbumActivity::class.java)

        }

        binding.imgHome.setOnClickListener {
            binding.recyclerFilter.visibility = View.INVISIBLE

            finish()
        }

        binding.imgFilter.setOnClickListener {
            binding.recyclerFilter.visibility = View.VISIBLE
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.beautiful)

            val adapter = FilterSelectionAdapter(originalBitmap, filterNames) { filter ->
                applyFilter(filter)
            }
            binding.recyclerFilter.adapter = adapter


        }
    }


    private fun setFlashIcon(camera: Camera) {

        if (camera.cameraInfo.hasFlashUnit()) {
            if (camera.cameraInfo.torchState.value == 0) {
                camera.cameraControl.enableTorch(true)
                binding.imgFlash.setImageResource(R.drawable.ic_flash_on)
            } else {
                camera.cameraControl.enableTorch(false)
                binding.imgFlash.setImageResource(R.drawable.ic_flash_off)
            }
        } else {
            binding.imgFlash.setImageResource(R.drawable.ic_flash_off)
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun takePhoto() {
        val imageFolder = File(cacheDir, "Images")
        if (!imageFolder.exists()) {
            imageFolder.mkdir()
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val imageFile = File(imageFolder, fileName)

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (lensFacing == CameraSelector.LENS_FACING_FRONT)
        }
        val outputOption = OutputFileOptions.Builder(imageFile)
            .setMetadata(metadata).build()

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    if (filter != null) {

                        val filterName =
                            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                                .format(System.currentTimeMillis()) + ".jpg"
                        val filteredImageFile = File(imageFolder, filterName)

                        try {
                            val filteredBitmap = binding.gpuimage.capture()
                            filteredBitmap?.let {
                                FileOutputStream(filteredImageFile).use { outputStream ->
                                    it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                }
                            }
                            imagePathSave = filteredImageFile.absolutePath
                            deleteImageFromInternalStorage(imageFile.absolutePath)
                            filter = null
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@TakePhotoActivity,
                                "Error saving filtered image: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        imagePathSave = imageFile.absolutePath
                    }

                    val intent =
                        Intent(this@TakePhotoActivity, HomeEditImageActivity::class.java).apply {
                            putExtra(Constants.KEY_IMAGE_PATH, imagePathSave)

                        }
                    startActivity(intent)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@TakePhotoActivity,
                        exception.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }


    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUserCases() {

        val rotation = binding.previewImage.display?.rotation ?: Surface.ROTATION_0

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.surfaceProvider = binding.previewImage.surfaceProvider
            }


        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(resolutionSelector)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    try {
                        val bitmap = imageProxy.toBitmap()
                        val rotatedBitmap = bitmap.rotateImage(imageProxy.imageInfo.rotationDegrees)

                        if (filter != null) {
                            binding.gpuimage.setImage(rotatedBitmap)
                            binding.gpuimage.filter = filter
                        } else {
                            binding.gpuimage.setImage(rotatedBitmap)
                            binding.gpuimage.filter = GPUImageFilter()

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        imageProxy.close()
                    }
                }

            }

        imageAnalyzer.targetRotation = rotation

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                imageCapture.targetRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
        orientationEventListener?.enable()
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer, imageCapture
            )
            setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun Bitmap.rotateImage(rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setUpZoomTapToFocus() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        binding.previewImage.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = binding.previewImage.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x - 60, y - 60, x + 60, y + 60)

                binding.focusCircleViewZoom.focusCircle = focusCircle
                binding.focusCircleViewZoom.invalidate()

                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }


        binding.gpuimage.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = binding.previewImage.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x - 60, y - 60, x + 60, y + 60)

                binding.focusCircleViewZoom.focusCircle = focusCircle
                binding.focusCircleViewZoom.invalidate()

                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }
    }

    fun applyFilter(filterName: String) {
        filter = when (filterName) {
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
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()
        observer()

    }

    private fun observer() {
        getViewModel().getData().observe(this) { list ->
            if (list.isNotEmpty()) {
                Glide.with(this)
                    .load(list[0])
                    .into(binding.imgViewImage)
            }

        }
        getViewModel().handlerLoadData()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()

    }


    private fun getViewModel(): AlbumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
}
