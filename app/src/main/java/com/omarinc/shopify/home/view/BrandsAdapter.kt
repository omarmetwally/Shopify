package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.BrandLayoutBinding
import com.omarinc.shopify.models.Brand


class BrandsAdapter(
    val context: Context,
   // private val listener: (weather: Favourites)->Unit,
    //private val favListener: (latitude:Double, longitude:Double)->Unit
) : ListAdapter<Brand, BrandsViewHolder>(
    FavouritesDiffUtil()
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
//        binding.brandImage = current.image
//        binding.brandName = current.name
        binding.brandName.text = current.name
        binding.brandImage.setImageResource(current.image)

//        binding.favConstraint.setOnClickListener {
//            favListener.invoke(
//                current.latitude,
//                current.longitude
//            )
//        }
    }
}

class BrandsViewHolder(val layout: BrandLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class FavouritesDiffUtil : DiffUtil.ItemCallback<Brand>() {
    override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem == newItem
    }

}