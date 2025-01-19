package com.example.photoedit.iu.album

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.photoedit.adapter.AlbumAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityAlbumBinding

class AlbumActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        val adapter = AlbumAdapter(this, listOf()) { imagePath, position ->
            val intent = Intent(this, ImageActivity::class.java).apply {
                putExtra(Constants.KEY_IMAGE_PATH, imagePath)
                putExtra(Constants.Key_POSITION, position)
            }
            startActivity(intent)
        }

        binding.recyclerPhoto.adapter = adapter



        getViewModel().getData().observe(this) { list ->
            adapter.images = list
            adapter.notifyDataSetChanged()


        }

    }

    override fun onResume() {
        super.onResume()
        getViewModel().handlerLoadData()
    }


    private fun getViewModel(): AlbumViewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
}