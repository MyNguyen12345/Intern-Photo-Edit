package com.example.photoedit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photoedit.databinding.RecyclerViewBinding

class ViewPagerAdapter(
    private val context: Context,
    private val folderList: List<String>,
    private val onClick: (String?) -> Unit
) : RecyclerView.Adapter<PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val binding = RecyclerViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return PagerViewHolder(binding,onClick)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val folderName = folderList[position]
        holder.bind(context, folderName)
    }

    override fun getItemCount(): Int = folderList.size
}
