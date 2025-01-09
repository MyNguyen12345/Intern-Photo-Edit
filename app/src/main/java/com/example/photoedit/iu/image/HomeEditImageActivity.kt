package com.example.photoedit.iu.image

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityHomeEditImageBinding
import com.example.photoedit.utils.intentActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream

class HomeEditImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeEditImageBinding
    private var imagePath: String? = null
    private var subscription: CompositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)


        Glide.with(this)
            .load(imagePath)
            .into(binding.imgViewImage)

        binding.btnBack.setOnClickListener {
            imagePath?.let {
                val isDeleted = deleteImageFromInternalStorage(it)
                if (isDeleted) {
                    Toast.makeText(this, "File deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete file or file does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            finish()
        }

        binding.btnShare.setOnClickListener {
            shareImage()

        }

        binding.btnSave.setOnClickListener {

            subscription.add(
                saveImageToGallery().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this, "save image $it", Toast.LENGTH_SHORT).show()

                    }, {
                        Toast.makeText(this, "don't save image $it", Toast.LENGTH_SHORT).show()

                    })
            )


        }

        binding.btnEdit.setOnClickListener {
            imagePath?.let { it1 ->
                intentActivity(
                    this, EditPhotoActivity::class.java, Constants.KEY_IMAGE_PATH,
                    it1
                )
            }
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
    private fun deleteImageFromInternalStorage(imagePath: String): Boolean {
        val file = File(imagePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }


    private fun saveImageToGallery(): Single<Uri> {
        return Single.create {
            imagePath?.let { path ->
                val file = File(path)
                val fileName = file.name

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH, "Pictures/images"
                    )
                }

                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                uri?.let { it1 ->
                    resolver.openOutputStream(it1).use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                    it.onSuccess(it1)

                } ?: run {
                    it.onError(IllegalAccessError())
                }
            }
        }


    }


}