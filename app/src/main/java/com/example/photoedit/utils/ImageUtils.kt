package com.example.photoedit.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

fun saveImage(context: Context, bitmap: Bitmap, imagePath: String): String? {
    var imagePathSave: String? = null
    val imageFolder = File(context.cacheDir, "Images")
    if (!imageFolder.exists()) {
        imageFolder.mkdir()
    }
    val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        .format(System.currentTimeMillis()) + ".jpg"
    val filteredImageFile = File(imageFolder, fileName)

    try {

        FileOutputStream(filteredImageFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        deleteImageFromInternalStorage(imagePath)
        imagePathSave = filteredImageFile.absolutePath

        Toast.makeText(
            context,
            "Filtered image saved: ${filteredImageFile.absolutePath}",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Error saving filtered image: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    return imagePathSave
}

fun dialogFinished(
    title: String,
    context: Context,
    imagePath: String?,
    onConfirmed: () -> Unit,
) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder
        .setTitle(title)
        .setPositiveButton("Yes") { dialog, _ ->
            imagePath?.let { deleteImageFromInternalStorage(it) }
            onConfirmed()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

    val dialog: AlertDialog = builder.create()
    dialog.show()
}

fun getAllImages(context: Context): List<String> {
    val imageList = mutableListOf<String>()
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
    val selectionArgs = arrayOf("%Pictures/images/%")

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )
    cursor?.use {
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        while (cursor.moveToNext()) {
            val imagePath = cursor.getString(columnIndex)
            imageList.add(imagePath)
        }
    }
    return imageList
}

fun saveImageToGallery(context: Context, bitmap: Bitmap): Single<Uri> {
    return Single.create { emitter ->

        val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/images")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let { savedUri ->
            try {
                resolver.openOutputStream(savedUri).use { outputStream ->
                    outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
                }
                emitter.onSuccess(savedUri)
            } catch (e: Exception) {
                resolver.delete(savedUri, null, null)
                emitter.onError(e)
            }
        } ?: run {
            emitter.onError(Exception("Failed to create MediaStore entry"))
        }
    }
}

fun getContentBounds(bitmap: Bitmap): Rect {
    val width = bitmap.width
    val height = bitmap.height
    var top = height
    var left = width
    var right = 0
    var bottom = 0

    for (y in 0 until height) {
        for (x in 0 until width) {
            if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                top = top.coerceAtMost(y)
                left = left.coerceAtMost(x)
                right = right.coerceAtLeast(x)
                bottom = bottom.coerceAtLeast(y)
            }
        }
    }

    return Rect(left, top, right, bottom)
}

fun getImageViewDimensions(imageView: ImageView): Pair<Int, Int> {
    val drawable = imageView.drawable ?: return Pair(0, 0)

    val intrinsicWidth = drawable.intrinsicWidth
    val intrinsicHeight = drawable.intrinsicHeight

    val imageViewWidth = imageView.width
    val imageViewHeight = imageView.height

    val scaleFactor =
        if (intrinsicWidth * imageViewHeight > intrinsicHeight * imageViewWidth) {
            imageViewWidth.toFloat() / intrinsicWidth
        } else {
            imageViewHeight.toFloat() / intrinsicHeight
        }

    val displayedWidth = (intrinsicWidth * scaleFactor).toInt()
    val displayedHeight = (intrinsicHeight * scaleFactor).toInt()

    return Pair(displayedWidth, displayedHeight)
}
