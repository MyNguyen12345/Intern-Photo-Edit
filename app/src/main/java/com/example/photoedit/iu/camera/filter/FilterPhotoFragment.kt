package com.example.photoedit.iu.camera.filter

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.photoedit.R
import com.example.photoedit.adapter.FilterSelectionAdapter
import com.example.photoedit.databinding.FragmentFilterPhotoBinding
import com.example.photoedit.iu.camera.TakePhotoActivity

class FilterPhotoFragment : Fragment() {

    private lateinit var binding: FragmentFilterPhotoBinding
    private val filterNames = listOf(
        "Original",
        "Sepia",
        "Contrast",
        "GrayScale",
        "GaussianBlur",
        "Brightness",
        "Saturation",
        "Invert",
        "Vignette"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterPhotoBinding.inflate(layoutInflater, container, false)
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.beautiful)

        val adapter = FilterSelectionAdapter(originalBitmap, filterNames) { filter ->
            (activity as? TakePhotoActivity)?.applyFilter(filter)
        }
        binding.recyclerFilter.adapter = adapter
        return binding.root
    }

}
