package com.example.photoedit.iu.album

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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
                    selectedImages.forEach { imagePath ->
                        deletePhotoFromMediaStore(imagePath)
                    }
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

    private fun deletePhotoFromMediaStore(filePath: String) {
        val file = File(filePath)
        val contentResolver = contentResolver
        val uriToDelete = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val rowsDeleted = contentResolver.delete(uriToDelete, selection, selectionArgs)

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Image deleted successfully from MediaStore", Toast.LENGTH_SHORT)
                .show()

        } else {
            Toast.makeText(this, "Failed to delete image from MediaStore", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun getViewModel(): AlbumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
}