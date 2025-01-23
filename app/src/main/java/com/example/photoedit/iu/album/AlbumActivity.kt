package com.example.photoedit.iu.album

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.photoedit.R
import com.example.photoedit.adapter.AlbumAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityAlbumBinding
import com.example.photoedit.utils.dialogFinished
import java.io.File

class AlbumActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumBinding
    private lateinit var adapter: AlbumAdapter
    private var isCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        adapter = AlbumAdapter(this, listOf(), getViewModel()) { imagePath, position ->
            Log.d("TAG", "onCreate: "+imagePath)
            val intent = Intent(this, ImageActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
                putExtra(Constants.Key_POSITION, position)
            }
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback {
            if (isCheck) {
                binding.btnDelete.visibility = View.INVISIBLE
                adapter.exitSelectionMode()
            } else {
                finish()
            }
        }


        binding.recyclerPhoto.adapter = adapter

        binding.btnDelete.setOnClickListener {
            val selectedImages = adapter.getSelectedImages()
            if (selectedImages.isNotEmpty()) {
                dialogFinished(getString(R.string.delete_dialog), this, null) {
                    deletePhotosFromMediaStore(selectedImages)

                    getViewModel().handlerLoadData()
                }
            } else {
                Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            }
        }



        getViewModel().getData().observe(this) { list ->
            adapter.updateImages(list)


        }
        getViewModel().isSelectionMode.observe(this) { isCheck ->
            this.isCheck = isCheck
            if (isCheck) {
                binding.btnDelete.visibility = View.VISIBLE
            } else {
                binding.btnDelete.visibility = View.INVISIBLE

            }
        }

    }

    override fun onResume() {
        super.onResume()
        getViewModel().handlerLoadData()

    }

    private fun deletePhotosFromMediaStore(imageUris: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver: ContentResolver = this.contentResolver
            try {
                val pendingIntent = MediaStore.createDeleteRequest(contentResolver, imageUris)
                startIntentSenderForResult(
                    pendingIntent.intentSender,
                    11, // Request code
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to delete images", Toast.LENGTH_SHORT).show()
            }
        } else {
            var deleteCount = 0
            for (uri in imageUris) {
                val rowsDeleted = contentResolver.delete(uri, null, null)
                if (rowsDeleted > 0) {
                    deleteCount++
                }
            }
            if (deleteCount == imageUris.size) {
                Toast.makeText(this, "All images deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete some images", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getViewModel(): AlbumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
}