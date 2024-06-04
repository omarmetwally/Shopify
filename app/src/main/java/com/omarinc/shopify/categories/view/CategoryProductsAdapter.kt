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
import com.omarinc.shopify.databinding.CategoriesLayoutBinding
import com.omarinc.shopify.databinding.CategoryProductsLayoutBinding
import com.omarinc.shopify.models.Brand
import com.omarinc.shopify.models.Product


class CategoryProductsAdapter(
    val context: Context,
   // private val listener: (weather: Favourites)->Unit,
    //private val favListener: (latitude:Double, longitude:Double)->Unit
) : ListAdapter<Product, CategoryProductsViewHolder>(
    CategoryProductsDiffUtil()
) {

    private lateinit var binding: CategoryProductsLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductsViewHolder {
         binding = CategoryProductsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductsViewHolder, position: Int) {
       // binding.favCard.setCardBackgroundColor( setCardViewBackground(context))
        val current = getItem(position)
//        binding.brandImage = current.image
//        binding.brandName = current.name
        binding.name.text = current.title
        Glide.with(context).load(current.imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.image)

//        binding.favConstraint.setOnClickListener {
//            favListener.invoke(
//                current.latitude,
//                current.longitude
//            )
//        }
    }
}

class CategoryProductsViewHolder(private val layout: CategoryProductsLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class CategoryProductsDiffUtil : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }

}