package com.example.photoedit.iu.image.frame

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.adapter.ViewPagerAdapter
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityFrameBinding
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.fixBitmapOrientation
import com.google.android.material.tabs.TabLayoutMediator


class FrameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrameBinding
    private var imagePath: String? = null

    private var backgroundImage: String? = null
    private var isCrop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)
        isCrop = intent.getBooleanExtra(Constants.Key_IS_CROP, false)
        Glide.with(this)
            .load(imagePath)
            .into(binding.imageShow)

        val folders = listOf("background/leave", "background/flowers")
        val titles = listOf("Leaves", "Flowers")

        val adapter = ViewPagerAdapter(this, folders) {
            backgroundImage = it
            if (it?.isNotEmpty() == true) {
                binding.imageBackground.visibility = View.VISIBLE
                val mainBitmap = BitmapFactory.decodeFile(imagePath)
                val bitmap = imagePath?.let { it1 -> fixBitmapOrientation(mainBitmap, it1) }
                val backgroundBitmap =
                    BitmapFactory.decodeStream(this.assets.open(it))
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

                binding.imageBackground.setImageBitmap(newBitmap)

            } else {
                binding.imageBackground.visibility = View.INVISIBLE
            }
        }
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()


        binding.btnBack.setOnClickListener {
            dialogFinished(getString(R.string.cancel_changes_dialog), this, null) { finish() }
        }

        onBackPressedDispatcher.addCallback {
            binding.btnBack.performClick()
        }
        binding.btnSave.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.KEY_IMAGE_BACKGROUND, backgroundImage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()

        }

    }
}
