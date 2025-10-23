package com.omarinc.shopify.home.view.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.AdsLayoutBinding
import com.omarinc.shopify.models.CouponDisplay
import com.omarinc.shopify.models.PriceRule


class AdsAdapter(
    private val context: Context,
    private val onItemLongClick: (PriceRule) -> Unit
) : ListAdapter<CouponDisplay, AdsAdapter.AdsViewHolder>(AdsDiffUtil()) {

    private lateinit var binding: AdsLayoutBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        binding = AdsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class AdsViewHolder(private val binding: AdsLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CouponDisplay) {
            binding.adImage.setImageResource(item.imageResId)
            binding.root.setOnLongClickListener {
                Log.i("AdsAdapter", "Long click on item: ${item.priceRule}")
                onItemLongClick(item.priceRule)
                true
            }
        }
    }
}

class AdsDiffUtil : DiffUtil.ItemCallback<CouponDisplay>() {
    override fun areItemsTheSame(oldItem: CouponDisplay, newItem: CouponDisplay): Boolean {
        return oldItem.priceRule.id == newItem.priceRule.id
    }

    override fun areContentsTheSame(oldItem: CouponDisplay, newItem: CouponDisplay): Boolean {
        return oldItem == newItem
    }
}

