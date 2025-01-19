package com.example.photoedit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photoedit.databinding.ItemStickerBinding
import com.example.photoedit.model.Sticker

class StickerAdapter(
    private val context: Context,
    var stickers: List<Sticker>,
    private val onClick: (Sticker, Int) -> Unit
) :
    RecyclerView.Adapter<StickerAdapter.StickerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val binding = ItemStickerBinding.inflate(LayoutInflater.from(context), parent, false)
        return StickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val sticker = stickers[position]

        holder.onBind(sticker, position)
    }

    override fun getItemCount(): Int = stickers.size

    inner class StickerViewHolder(private val binding: ItemStickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(sticker: Sticker, position: Int) {

            binding.apply {
                Glide.with(context)
                    .load(sticker.stickerPath)
                    .into(imageSticker)


                if (sticker.isDownload) {
                    binding.btnSave.visibility = View.INVISIBLE
                } else {
                    binding.btnSave.visibility = View.VISIBLE
                }


                containerLayout.setOnClickListener {
                    onClick.invoke(sticker, position)

                }
            }
        }
    }
}
