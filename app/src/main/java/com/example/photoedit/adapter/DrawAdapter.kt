package com.example.photoedit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoedit.databinding.ItemDrawBinding
import com.example.photoedit.databinding.ItemStickerBinding
import com.example.photoedit.model.Sticker

class DrawAdapter(
    private val context: Context,
    var backgroundDraw: List<Int>,
    private val onClick: (Int, Int) -> Unit
) :
    RecyclerView.Adapter<DrawAdapter.DrawViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawViewHolder {
        val binding = ItemDrawBinding.inflate(LayoutInflater.from(context), parent, false)
        return DrawViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DrawViewHolder, position: Int) {
        val bcDraw = backgroundDraw[position]

        holder.onBind(bcDraw, position)
    }

    override fun getItemCount(): Int = backgroundDraw.size

    inner class DrawViewHolder(private val binding: ItemDrawBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(bcDraw:Int, position: Int) {

            binding.apply {
                Glide.with(context)
                    .load(bcDraw)
                    .into(imageSticker)

                containerLayout.setOnClickListener {
                    onClick.invoke(bcDraw, position)

                }
            }
        }
    }
}
