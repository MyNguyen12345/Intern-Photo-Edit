package com.example.photoedit.iu.image

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityEditPhotoBinding
import com.isseiaoki.simplecropview.callback.CropCallback
import com.isseiaoki.simplecropview.callback.SaveCallback

class EditPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPhotoBinding
    private var imagePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)


        Glide.with(this)
            .load(imagePath)
            .into(binding.imgViewImage)
        val mCropView = binding.cropImageView
        binding.btnCrop.setOnClickListener {


            //        mCropView.load(Uri.parse(imagePath)).executeAsCompletable()
            mCropView.crop(Uri.parse(imagePath))
                .execute(object : CropCallback {
                    override fun onSuccess(cropped: Bitmap) {
                        mCropView.save(cropped)
                            .execute(Uri.parse(imagePath), object : SaveCallback {
                                override fun onSuccess(uri: Uri) {
                                    // Thao tác khi lưu thành công
                                }

                                override fun onError(e: Throwable) {
                                    // Thao tác khi có lỗi trong quá trình lưu
                                }
                            })
                    }

                    override fun onError(e: Throwable) {
                        // Thao tác khi xảy ra lỗi khi crop
                    }
                })


        }


    }
}