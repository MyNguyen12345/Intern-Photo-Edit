package com.example.photoedit.iu.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.photoedit.R
import com.example.photoedit.adapter.ImagePagerAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityImageBinding
import com.example.photoedit.utils.dialogFinished
import java.io.File

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    private var imagePath: String? = null
    private var listImage = listOf<String>()
    private var positionImage: Int = -1
    private var sizeImage = 0
    private lateinit var imageAdapter: ImagePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)
        positionImage = intent.getIntExtra(Constants.Key_POSITION, -1)

        observe()
        imageAdapter = ImagePagerAdapter()
        binding.viewPager.adapter = imageAdapter
        binding.viewPager.currentItem = positionImage

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.imgDelete.setOnClickListener {
            dialogFinished(getString(R.string.delete_dialog), this, null) {
                imagePath?.let { it1 -> deletePhotoFromMediaStore(it1) }
            }
        }
    }

    private fun deletePhotoFromMediaStore(filePath: String) {
        val file = File(filePath)
        val contentResolver = contentResolver
        val uriToDelete = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val rowsDeleted = contentResolver.delete(uriToDelete, selection, selectionArgs)

        if (rowsDeleted > 0) {
            getViewModel().handlerLoadData()
            sizeImage = listImage.size

            if (positionImage >= sizeImage) {
                positionImage = sizeImage - 1
            }
            Toast.makeText(this, "Image deleted successfully from MediaStore", Toast.LENGTH_SHORT)
                .show()

        } else {
            Toast.makeText(this, "Failed to delete image from MediaStore", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun observe() {
        getViewModel().getData().observe(this) { list ->
            listImage = list
            if (positionImage in listImage.indices) {
                imagePath = listImage[positionImage]
                imageAdapter.submitList(listImage)
                binding.viewPager.currentItem = positionImage

            } else {
                finish()
            }


        }
        getViewModel().handlerLoadData()

    }

    private fun getViewModel(): AlbumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
}
