package com.example.photoedit.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photoedit.databinding.ItemFilterPhotoBinding
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter

class FilterSelectionAdapter(
    private val bitmap: Bitmap,
    private val filters: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FilterSelectionAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFilterPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filter = filters[position]
        holder.onBind(filter, position)
    }

    override fun getItemCount(): Int = filters.size

    inner class ViewHolder(private val binding: ItemFilterPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(filterName: String, position: Int) {
            binding.apply {
                tvFilter.text = filterName
                val gpuImage = GPUImage(itemView.context)
                gpuImage.setImage(bitmap)

                val filter = when (filterName) {
                    "Original" -> null
                    "Sepia" -> GPUImageSepiaToneFilter()
                    "Contrast" -> GPUImageContrastFilter(2.0f)
                    "GrayScale" -> GPUImageGrayscaleFilter()
                    "GaussianBlur" -> GPUImageGaussianBlurFilter()
                    "Brightness" -> GPUImageBrightnessFilter(0.5f)
                    "Saturation" -> GPUImageSaturationFilter(2.0f)
                    "Invert" -> GPUImageColorInvertFilter()
                    "Vignette" -> GPUImageVignetteFilter()
                    else -> null
                }
                if (filter != null) {
                    gpuImage.setFilter(filter)

                    val filteredBitmap = gpuImage.bitmapWithFilterApplied
                    imageFilter.setImageBitmap(filteredBitmap)
                } else {
                    imageFilter.setImageBitmap(bitmap)
                }

                itemFilter.setOnClickListener {
                    onClick.invoke(filterName)
                }
            }

        }
    }
}
