package com.example.photoedit.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.photoedit.model.Sticker
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import java.io.File


fun getStickersFromFirebase(
    context: Context,
    onSuccess: (List<Sticker>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    val stickersReference: StorageReference = storageReference.child("stickers")

    stickersReference.listAll()
        .addOnSuccessListener { listResult: ListResult ->
            val imageUris = mutableListOf<Sticker>()

            val items = listResult.items
            for (item in items) {
                item.downloadUrl.addOnSuccessListener { uri ->
                    val imagePathInStorage = getStoragePathFromUrl(uri.toString())


                    val localFile = File(context.filesDir, imagePathInStorage)

                    imageUris.add(Sticker(uri, localFile.exists()))

                    if (imageUris.size == items.size) {
                        onSuccess(imageUris)
                    }
                }.addOnFailureListener {
                    onFailure(it)
                }
            }
        }
        .addOnFailureListener {
            onFailure(it)
        }
}


fun saveImageToInternalStorage(imageUri: Uri, context: Context): String? {
    var imagePath: String? = null
    val storageReference = FirebaseStorage.getInstance().reference
    val imagePathInStorage = getStoragePathFromUrl(imageUri.toString())

    val imageRef = storageReference.child(imagePathInStorage)

    val localFile = File(context.filesDir, imagePathInStorage)

    localFile.parentFile?.mkdirs()

    imageRef.getFile(localFile).addOnSuccessListener {
        Log.d("FirebaseStorage", "Ảnh đã được lưu vào: ${localFile.absolutePath}")
        imagePath = localFile.absolutePath
    }.addOnFailureListener { exception ->
        Log.e("FirebaseStorage", "Lỗi tải file: ${exception.message}")
    }
    return imagePath
}

fun getStoragePathFromUrl(imageUrl: String): String {
    return imageUrl.substringAfter("o/")
        .substringBefore("?")
        .replace("%2F", "/")
}
