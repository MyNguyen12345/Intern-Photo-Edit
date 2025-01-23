package com.example.photoedit.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photoedit.databinding.ItemAlbumImageBinding
import com.example.photoedit.utils.getPathFromUri

class ImagePagerAdapter(   private val onClick: (String) -> Unit) :
    ListAdapter<Uri, ImagePagerAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemAlbumImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = getItem(position)

        Glide.with(holder.itemView.context).load(imagePath)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.binding.progressCircular.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.binding.progressCircular.visibility = View.GONE
                    return false
                }

            }).into(holder.binding.imageView)

        holder.binding.imageView.setOnClickListener {
            getPathFromUri(holder.itemView.context,imagePath)?.let { it1 -> onClick.invoke(it1) }
        }

    }

    inner class ImageViewHolder(val binding: ItemAlbumImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ImageDiffCallback : DiffUtil.ItemCallback<Uri>() {

        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem        }
    }
}
