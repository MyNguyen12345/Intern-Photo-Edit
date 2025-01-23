package com.example.photoedit.iu.album

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.photoedit.R
import com.example.photoedit.adapter.ImagePagerAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityImageBinding
import com.example.photoedit.iu.image.EditPhotoActivity
import com.example.photoedit.utils.dialogFinished
import java.io.File

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    private var imagePath: Uri? = null
    private var listImage = listOf<Uri>()
    private var positionImage: Int = -1
    private var sizeImage = 0
    private lateinit var imageAdapter: ImagePagerAdapter
    private val requestCodeDelete = 11
    private var isDelete = false

    private val deleteImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                isDelete = true
                deletePhotoFromMediaStore()
            } else {
                isDelete = false
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        positionImage = intent.getIntExtra(Constants.Key_POSITION, -1)

        observe()
        imageAdapter = ImagePagerAdapter { imagePath ->
            Log.d("TAG", "onCreate: "+imagePath)
            val intent = Intent(this, EditPhotoActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
            }
            startActivity(intent)
        }
        binding.viewPager.adapter = imageAdapter
        binding.viewPager.currentItem = positionImage

        binding.btnBack.setOnClickListener {
            finish()
        }


        binding.imgDelete.setOnClickListener {
            dialogFinished(getString(R.string.delete_dialog), this, null) {
                imagePath?.let { it1 ->   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    deleteAPI30(it1)
                } else {
                    deleteAPI28(it1)
                }
                }
            }
        }
    }

    private fun deletePhotoFromMediaStore() {

        if (isDelete) {
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





    private fun deleteAPI28(imageUri: Uri) {
        val resolver = this.contentResolver
        val rowsDeleted = resolver.delete(imageUri, null, null)
        isDelete = rowsDeleted > 0
        deletePhotoFromMediaStore()
    }



    @RequiresApi(Build.VERSION_CODES.R)
    @Throws(IntentSender.SendIntentException::class)
    private fun deleteAPI30(imageUri: Uri) {
        val contentResolver: ContentResolver = this.contentResolver
        val uriList = mutableListOf(imageUri)

        try {
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uriList)
            deleteImageLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
