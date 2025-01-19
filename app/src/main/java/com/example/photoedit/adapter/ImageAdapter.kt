package com.example.photoedit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoedit.R
import com.example.photoedit.databinding.ImageItemBinding

class ImageAdapter(
    private val context: Context,
    private val imagePaths: List<String>,
    private val onClick: (String?) -> Unit
) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val path = imagePaths[position]

        holder.onBind(path, position)
    }

    override fun getItemCount(): Int = imagePaths.size

    inner class ImageViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(path: String, position: Int) {
            val assetPath = "file:///android_asset/$path"

            binding.apply {
                Glide.with(binding.root.context)
                    .load(assetPath)
                    .into(imageView)

                Glide.with(binding.root.context)
                    .load(R.drawable.beautiful)
                    .into(imageShow)
                frameContainer.setOnClickListener {
                    if (position == 0) {
                        onClick.invoke(null)

                    } else {
                        onClick.invoke(path)

                    }
                }
            }
        }
    }
}
