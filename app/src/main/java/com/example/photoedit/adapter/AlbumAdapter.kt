package com.example.photoedit.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photoedit.databinding.ItemAlbumBinding
import com.example.photoedit.iu.album.AlbumViewModel

class AlbumAdapter(
    private val context: Context,
    private var images: List<Uri>,
    private val viewModel: AlbumViewModel,
    private val onClick: (String, Int) -> Unit
) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private val selectedItems = mutableListOf<Boolean>().apply {
        repeat(images.size) { add(false) }
    }

    private var isSelectionMode = false


    fun updateImages(newImages: List<Uri>) {
        images = newImages
        selectedItems.clear()
        selectedItems.addAll(List(newImages.size) { false })
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(image: Uri, position: Int) {
            binding.apply {
                Glide.with(context).load(image).centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressCircular.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressCircular.visibility = View.GONE
                            return false
                        }
                    }).into(imageGallery)

                imageGallery.setOnClickListener {
                    if (isSelectionMode) {
                        selectedItems[position] = !selectedItems[position]
                        checkbox.isChecked = selectedItems[position]
                    } else {
                        onClick.invoke(image.path.toString(), position)
                    }
                }
                if (selectedItems.isNotEmpty()) {
                    checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.INVISIBLE
                    checkbox.isChecked = selectedItems[position]

                    imageGallery.setOnLongClickListener {

                        isSelectionMode = true
                        viewModel.isSelectionMode.value = true
                        notifyDataSetChanged()
                        true
                    }

                    checkbox.setOnClickListener {
                        selectedItems[position] = checkbox.isChecked
                    }

                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(images[position], position)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.fill(false)
        viewModel.isSelectionMode.value = false
        notifyDataSetChanged()
    }

    fun getSelectedImages(): List<Uri> {
        return images.filterIndexed { index, _ -> selectedItems[index] }
    }
}
