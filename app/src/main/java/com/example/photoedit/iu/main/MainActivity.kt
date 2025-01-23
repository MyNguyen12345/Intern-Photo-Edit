package com.example.photoedit.iu.main

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.photoedit.R
import com.example.photoedit.constants.Constants
import com.example.photoedit.databinding.ActivityMainBinding
import com.example.photoedit.iu.album.AlbumActivity
import com.example.photoedit.iu.camera.TakePhotoActivity
import com.example.photoedit.utils.SharedPreferencesUtils
import com.example.photoedit.utils.appSettingOpen
import com.example.photoedit.utils.dialogFinished
import com.example.photoedit.utils.intentActivity
import com.example.photoedit.utils.warningPermissionDialog

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferencesUtils: SharedPreferencesUtils
    private lateinit var permissionsRequestLauncher: ActivityResultLauncher<Array<String>>
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferencesUtils = SharedPreferencesUtils(this)
        setContentView(binding.root)
        checkPermission()
        checkMultiplePermission()

        binding.layoutEdit.setOnClickListener {
            intentActivity(this, TakePhotoActivity::class.java)
        }

        binding.layoutAlbum.setOnClickListener {

            intentActivity(this, AlbumActivity::class.java)
        }






        onBackPressedDispatcher.addCallback {
            dialogFinished(
                getString(R.string.cancel_app_dialog),
                this@MainActivity,
                null
            ) { finishAffinity() }


        }
    }

    private fun checkPermission() {
        permissionsRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.all { it }

            if (granted) {
                sharedPreferencesUtils.saveBoolean(Constants.KEY_CHECK_PERMISSION, true)
                return@registerForActivityResult
            }

            val deniedPermissions = permissions.filter { !it.value }.keys
            val canRequestPermissions = deniedPermissions.filter { permission ->
                shouldShowRequestPermissionRationale(permission)
            }
            val permanentlyDeniedPermissions = deniedPermissions.filterNot { permission ->
                shouldShowRequestPermissionRationale(permission)
            }

            if (permanentlyDeniedPermissions.isNotEmpty()) {
                appSettingOpen(this)
            } else if (canRequestPermissions.isNotEmpty()) {
                warningPermissionDialog(this) { _, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        permissionsRequestLauncher.launch(canRequestPermissions.toTypedArray())
                    }
                }
            }
        }

    }


    private fun checkMultiplePermission() {
        val granted = multiplePermissionNameList.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (granted) {
            sharedPreferencesUtils.saveBoolean(Constants.KEY_CHECK_PERMISSION, true)
        } else {
            permissionsRequestLauncher.launch(multiplePermissionNameList)
        }
    }
}
