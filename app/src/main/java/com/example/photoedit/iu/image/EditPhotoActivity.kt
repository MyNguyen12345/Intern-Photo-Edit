package com.example.photoedit.iu.image

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photoedit.R
import com.example.photoedit.adapter.StickerAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityEditPhotoBinding
import com.example.photoedit.databinding.DialogSaveBinding
import com.example.photoedit.firebase.getStickersFromFirebase
import com.example.photoedit.firebase.getStoragePathFromUrl
import com.example.photoedit.firebase.saveImageToInternalStorage
import com.example.photoedit.iu.album.AlbumActivity
import com.example.photoedit.iu.camera.TakePhotoActivity
import com.example.photoedit.iu.image.color.ColorImageActivity
import com.example.photoedit.iu.image.draw.DrawPhotoActivity
import com.example.photoedit.iu.image.frame.FrameActivity
import com.example.photoedit.iu.main.MainActivity
import com.example.photoedit.iu.view.CustomEditTextView
import com.example.photoedit.iu.view.CustomImageView
import com.example.photoedit.model.Sticker
import com.example.photoedit.utils.deleteImageFromInternalStorage
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.fixBitmapOrientation
import com.example.photoedit.utils.getContentBounds
import com.example.photoedit.utils.getImageViewDimensions
import com.example.photoedit.utils.intentActivity
import com.example.photoedit.utils.saveImageToGallery
import com.yalantis.ucrop.UCrop
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class EditPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPhotoBinding
    private var imagePath: String? = null
    private var backgroundImage: String? = null
    private var stickers: MutableList<Sticker> = mutableListOf()
    private var sticker = Sticker()
    private var positionSticker = -1
    private var adapter: StickerAdapter? = null
    private var subscription: CompositeDisposable = CompositeDisposable()
    private lateinit var dialog: AlertDialog

    private lateinit var cropImageLauncher: ActivityResultLauncher<Intent>
    private val editTextViews: MutableList<CustomEditTextView> = mutableListOf()


    @SuppressLint("SuspiciousIndentation")
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                backgroundImage = result.data?.getStringExtra(Constants.KEY_IMAGE_BACKGROUND)
                val imagePath = result.data?.getStringExtra(Constants.KEY_IMAGE_PATH)
                if (imagePath != null) {
                    this.imagePath = imagePath
                    setColorImage()
                }
                setBackgroundImage()
            }
        }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)




        binding.btnCrop.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE
            startImageCrop()

        }


        binding.btnBack.setOnClickListener {
            dialogFinished(getString(R.string.cancel_changes_dialog), this, null) { finish() }
        }

        onBackPressedDispatcher.addCallback {
            binding.btnBack.performClick()
        }

        binding.btnFrame.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE

            val intent = Intent(this@EditPhotoActivity, FrameActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
            }
            resultLauncher.launch(intent)
        }
        binding.btnEdit.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE

            val intent = Intent(this@EditPhotoActivity, DrawPhotoActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
                putExtra(Constants.KEY_IMAGE_BACKGROUND, backgroundImage)
            }
            resultLauncher.launch(intent)
        }
        binding.btnSticker.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE


            getStickersFromFirebase(
                this,
                onSuccess = { stickers ->
                    this.stickers = stickers.toMutableList()
                    adapter = StickerAdapter(this, this.stickers) { sticker, position ->
                        this.sticker = sticker
                        positionSticker = position
                        setSticker()
                    }
                    binding.recyclerSticker.adapter = adapter
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.recyclerSticker.visibility = View.VISIBLE
                    adapter?.notifyDataSetChanged()
                },
                onFailure = { _ ->
                    Toast.makeText(this,"Download failed",Toast.LENGTH_SHORT).show()
                }
            )

        }

        binding.btnColor.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE

            val intent = Intent(this@EditPhotoActivity, ColorImageActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
                putExtra(Constants.KEY_IMAGE_BACKGROUND, backgroundImage)

            }
            resultLauncher.launch(intent)
        }

        binding.btnText.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE

            val newEditTextView = CustomEditTextView(this, null).apply {
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

                hint = getString(R.string.hint_text)
                setHintTextColor(Color.WHITE)
                setTextColor(Color.WHITE)
                textSize = 18f
                setBackgroundColor(Color.TRANSPARENT)
            }

            binding.containerLayout.addView(newEditTextView)
            editTextViews.add(newEditTextView)


        }

        binding.btnSave.setOnClickListener {
            binding.recyclerSticker.visibility = View.INVISIBLE
            addTextToImage()
           dialogFinished(getString(R.string.do_home),this,imagePath){
               intentActivity(this@EditPhotoActivity, MainActivity::class.java)

           }
        }

        binding.btnDelete.setOnClickListener {
            dialogFinished(getString(R.string.delete_dialog), this, imagePath) {
                finish()
            }
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

                    imagePath = resultUri?.path.toString()
                    setBackgroundImage()


                } else if (result.resultCode == UCrop.RESULT_ERROR) {
                    val cropError = UCrop.getError(result.data!!)
                    Toast.makeText(
                        this,
                        "Error cropping image: ${cropError?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun setSticker() {
        val imagePathInStorage = getStoragePathFromUrl(sticker.stickerPath.toString())

        val localFile = File(filesDir, imagePathInStorage)


        if (localFile.exists()) {
            val newImageView = CustomImageView(this, null).apply {
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


            }

            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            newImageView.setImageBitmap(bitmap)
            binding.containerLayout.addView(newImageView)


        } else {
            if (!sticker.isDownload) {
                dialogFinished(getString(R.string.download_dialog), this, null) {
                    sticker.stickerPath?.let { it1 -> saveImageToInternalStorage(it1, this) }
                    sticker.isDownload = true
                    stickers[positionSticker] = sticker
                    adapter?.stickers = stickers
                    adapter?.notifyDataSetChanged()
                }
            }


        }
    }

    fun setLayoutContainer() {
        binding.containerLayout.post {
            val (displayedWidth, displayedHeight) = getImageViewDimensions(binding.imgViewImage)
            if (displayedWidth > 0 && displayedHeight > 0) {
                val layoutParams = binding.containerLayout.layoutParams
                layoutParams.width = displayedWidth
                layoutParams.height = displayedHeight
                binding.containerLayout.layoutParams = layoutParams
                binding.containerLayout.requestLayout()
            }
        }
    }


    private fun setColorImage() {
        Glide.with(this)
            .load(imagePath)
            .into(binding.imgViewImage)

    }

    private fun setBackgroundImage() {

        if (backgroundImage?.isNotEmpty() == true) {
            binding.imgViewBackground.visibility = View.VISIBLE
            val mainBitmap = BitmapFactory.decodeFile(imagePath)
            val bitmap = imagePath?.let { it1 -> fixBitmapOrientation(mainBitmap, it1) }
            val backgroundBitmap =
                BitmapFactory.decodeStream(this.assets.open(backgroundImage!!))
            val scaledBackgroundBitmap = bitmap?.let { it1 ->
                Bitmap.createScaledBitmap(
                    backgroundBitmap,
                    it1.width,
                    it1.height,
                    true
                )
            }


            val newBitmap = bitmap?.let { it1 ->
                Bitmap.createBitmap(
                    it1.width,
                    it1.height,
                    mainBitmap.config
                )
            }

            val canvas = newBitmap?.let { it1 -> Canvas(it1) }
            scaledBackgroundBitmap?.let { it1 -> canvas?.drawBitmap(it1, 0f, 0f, null) }

            binding.imgViewBackground.setImageBitmap(newBitmap)

        } else {
            binding.imgViewBackground.visibility = View.INVISIBLE
        }
    }


    private fun startImageCrop() {
        imagePath?.let { path ->
            var sourceUri: Uri? = null

            val sourceFile = File(path)
            sourceUri = if (sourceFile.exists()) {
                FileProvider.getUriForFile(
                    this,
                    "com.example.photoedit.provider",
                    sourceFile
                )
            } else {
                Uri.parse(path)
            }


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

    override fun onResume() {
        super.onResume()
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
                    setLayoutContainer()
                    return false
                }

            })
            .into(binding.imgViewImage)

    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus

        if (view is CustomEditTextView) {
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

        val contentBounds = getContentBounds(bitmap)

        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            contentBounds.left,
            contentBounds.top,
            contentBounds.width(),
            contentBounds.height()
        )

        subscription.add(
            saveImageToGallery(this, croppedBitmap).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(this, "save image $it", Toast.LENGTH_SHORT).show()

                }, {
                    Toast.makeText(this, "don't save image $it", Toast.LENGTH_SHORT).show()

                })
        )

    }

    private fun dialogSave() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val binding = DialogSaveBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        binding.apply {
            btnNo.setOnClickListener { dialog.dismiss() }
            btnPhoto.setOnClickListener {
                imagePath?.let { it1 -> deleteImageFromInternalStorage(it1) }
                intentActivity(this@EditPhotoActivity, TakePhotoActivity::class.java)
                dialog.dismiss()
                finish()
            }
            btnAlbum.setOnClickListener {
                imagePath?.let { it1 -> deleteImageFromInternalStorage(it1) }
                intentActivity(this@EditPhotoActivity, AlbumActivity::class.java)
                dialog.dismiss()
                finish()

            }
        }

        dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.clear()
    }

}
