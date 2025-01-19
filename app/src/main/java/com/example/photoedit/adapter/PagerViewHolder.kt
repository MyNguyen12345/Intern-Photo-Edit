package com.example.photoedit.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.photoedit.databinding.RecyclerViewBinding

class PagerViewHolder(binding: RecyclerViewBinding, private val onClick: (String?) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {

    private val recyclerView: RecyclerView = binding.recyclerView

    fun bind(context: Context, folder: String) {
        val images = loadImagesFromAssets(context, folder)
        recyclerView.adapter = ImageAdapter(context, images) {
            onClick.invoke(it)
        }
    }

    private fun loadImagesFromAssets(context: Context, folder: String): List<String> {
        return try {
            val assets = context.assets.list(folder) ?: emptyArray()
            assets.map { "$folder/$it" }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
