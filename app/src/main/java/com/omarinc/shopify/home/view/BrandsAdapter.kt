package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.BrandLayoutBinding
import com.omarinc.shopify.models.Brands


class BrandsAdapter(
    val context: Context,
    private val listener: (id: String)->Unit,
) : ListAdapter<Brands, BrandsViewHolder>(
    BrandsDiffUtil()
) {

    private lateinit var binding: BrandLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
         binding = BrandLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BrandsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
       // binding.favCard.setCardBackgroundColor( setCardViewBackground(context))
        val current = getItem(position)

        binding.brandName.text = current.title
        Glide.with(context).load(current.imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.brandImage)
        binding.planConstrainLayout.setOnClickListener {
            listener.invoke(current.id)

        }
    }
}

class BrandsViewHolder(val layout: BrandLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class BrandsDiffUtil : DiffUtil.ItemCallback<Brands>() {
    override fun areItemsTheSame(oldItem: Brands, newItem: Brands): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Brands, newItem: Brands): Boolean {
        return oldItem == newItem
    }

}