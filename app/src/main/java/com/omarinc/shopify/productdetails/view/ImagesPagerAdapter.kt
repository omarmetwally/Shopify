package com.omarinc.shopify.productdetails.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ItemImageBinding

class ImagesPagerAdapter(
    private val context: Context,
    private val imageUrls: List<Any>,
    private val onImageClick: (Any) -> Unit
) : RecyclerView.Adapter<ImagesPagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: Any) {
            Glide.with(context).load(imageUrl).into(binding.imageView)
            binding.imageView.setOnClickListener {
                onImageClick(imageUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}
