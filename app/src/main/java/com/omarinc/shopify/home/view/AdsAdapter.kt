package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.AdsLayoutBinding
import com.omarinc.shopify.databinding.BrandLayoutBinding
import com.omarinc.shopify.models.Brand


class AdsAdapter(
    val context: Context,
) : ListAdapter<Int, AdsViewHolder>(
    AdsDiffUtil()
) {

    private lateinit var binding: AdsLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
         binding = AdsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        val current = getItem(position)
        binding.adImage.setImageResource(current)

    }
}

class AdsViewHolder(val layout: AdsLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class AdsDiffUtil : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

}