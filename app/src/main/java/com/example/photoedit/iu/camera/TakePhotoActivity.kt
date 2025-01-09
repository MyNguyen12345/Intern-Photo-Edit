package com.example.photoedit.iu.camera

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
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
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.photoedit.R
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityTakePhotoBinding
import com.example.photoedit.iu.album.AlbumActivity
import com.example.photoedit.iu.camera.filter.FilterPhotoFragment
import com.example.photoedit.iu.image.HomeEditImageActivity
import com.example.photoedit.model.Filter
import com.example.photoedit.utils.SharedPreferencesUtils
import com.example.photoedit.utils.intentActivity
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import java.io.ByteArrayOutputStream
import java.io.File
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
    private lateinit var gpuImage: GPUImage


    private lateinit var binding: ActivityTakePhotoBinding
    private  lateinit var  surface  : Surface



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferencesUtils = SharedPreferencesUtils(this)

        if (sharedPreferencesUtils.isBoolean(Constants.KEY_CHECK_PERMISSION)) {
            gpuImage = GPUImage(this)
//            gpuImage.setGLSurfaceView(binding.gpuimage)
            startCamera()

        } else {
            Toast.makeText(this, "Check permission ", Toast.LENGTH_SHORT).show()
        }
        setAspectRatio("H,9:16")


        binding.imgFlip.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUserCases()
        }

        binding.imgRatio.setOnClickListener {
            if (aspectRatio == AspectRatio.RATIO_16_9) {
                aspectRatio = AspectRatio.RATIO_4_3
                setAspectRatio("H,3:4")
                binding.imgRatio.setImageResource(R.drawable.ic_four_three)
            } else {
                aspectRatio = AspectRatio.RATIO_16_9
                setAspectRatio("H,9:16")
                binding.imgRatio.setImageResource(R.drawable.ic_sixteen_nine)

            }
            bindCameraUserCases()
        }

        binding.imgPhoto.setOnClickListener {
            takePhoto()

        }

        binding.imgFlash.setOnClickListener {
            setFlashIcon(camera)

        }
        binding.cardImage.setOnClickListener {
            intentActivity(this, AlbumActivity::class.java)

        }

        binding.imgHome.setOnClickListener {
            finish()
        }

        binding.imgFilter.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(binding.frameContainer.id,FilterPhotoFragment())
                .commit()

        }

    }

    private fun setAspectRatio(ratio: String) {
        binding.previewImage.layoutParams = binding.previewImage.layoutParams.apply {
            if (this is ConstraintLayout.LayoutParams) {
                dimensionRatio = ratio
            }
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
                    val intent = Intent(this@TakePhotoActivity, HomeEditImageActivity::class.java).apply {
                        putExtra(Constants.KEY_IMAGE_PATH, imageFile.absolutePath)

                    }
                    startActivity(intent)
//                    Glide.with(this@TakePhotoActivity)
//                        .load(imageFile)
//                        .centerCrop()
//                        .signature(ObjectKey(System.currentTimeMillis()))
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .into(binding.imgViewImage)
//
//                    Toast.makeText(
//                        this@TakePhotoActivity,
//                        "Image saved temporarily: ${imageFile.absolutePath}",
//                        Toast.LENGTH_SHORT
//                    ).show()
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
        val rotation = binding.previewImage.display.rotation

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
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    processImage(imageProxy)
                }
            }

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
                // Monitors orientation values to determine the target rotation value
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
    private fun processImage(imageProxy: ImageProxy) {
        val bitmap = yuv420ToBitmap(imageProxy) // Chuyển đổi từ YUV sang Bitmap
        if (bitmap != null) {
            val convertedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            // Áp dụng bộ lọc bằng GPUImage
//            gpuImage.setImage(convertedBitmap)
//            gpuImage.setFilter(GPUImageBrightnessFilter(0.5f)) // Áp dụng bộ lọc
//            gpuImage.requestRender()
//            val filteredBitmap = gpuImage.bitmapWithFilterApplied

//            // Vẽ Bitmap lên TextureView
//            val canvas = binding.previewImage.lockCanvas()
//            canvas?.drawBitmap(bitmap, null, Rect(0, 0, canvas.width, canvas.height), null)
//            canvas?.let { binding.previewImage.unlockCanvasAndPost(it) }
        }
        imageProxy.close()
    }

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
    }

    private fun yuv420ToBitmap(imageProxy: ImageProxy): Bitmap? {
        if (imageProxy.format != ImageFormat.YUV_420_888) {
            Log.e("TAG", "Invalid image format")
            return null
        }

        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val yuvImage = YuvImage(
            bytes,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )
        val outStream = ByteArrayOutputStream()
        val rect = Rect(0, 0, imageProxy.width, imageProxy.height)
        yuvImage.compressToJpeg(rect, 100, outStream)
        val imageBytes = outStream.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
     fun applyFilter(filterName: String) {
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
         Log.d("TAG", "applyFilter: "+filter)
         if (filter != null) {
//             gpuImage.setFilter(filter)
//             gpuImage.requestRender()
             Log.d("TAG", "applyFilter: "+gpuImage)
         }

    }

    override fun onResume() {
        super.onResume()
        orientationEventListener?.enable()


    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()

    }
}