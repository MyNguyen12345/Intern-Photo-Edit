package com.example.photoedit.utils

import android.R
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun appSettingOpen(context: Context) {
    Toast.makeText(
        context,
        "Go to Setting and Enable All Permission",
        Toast.LENGTH_LONG
    ).show()

    val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    settingIntent.data = Uri.parse("package:${context.packageName}")
    context.startActivity(settingIntent)
}

fun warningPermissionDialog(context: Context, listener: DialogInterface.OnClickListener) {
    MaterialAlertDialogBuilder(context)
        .setMessage("All Permission are Required for this app")
        .setCancelable(false)
        .setPositiveButton("Ok", listener)
        .create()
        .show()
}

fun intentActivity(context: Context,className: Class<*>){
    val intent = Intent(context, className)
    context.startActivity(intent)
}

fun intentActivity(context: Context,className: Class<*>,key:String,string : String){
    val intent = Intent(context, className).apply {
        putExtra(key,string)
    }
    context.startActivity(intent)
}


