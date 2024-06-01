package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.BrandLayoutBinding
import com.omarinc.shopify.databinding.CategoriesLayoutBinding
import com.omarinc.shopify.databinding.CategoryProductsLayoutBinding
import com.omarinc.shopify.models.Brand


class CategoryProductsAdapter(
    val context: Context,
   // private val listener: (weather: Favourites)->Unit,
    //private val favListener: (latitude:Double, longitude:Double)->Unit
) : ListAdapter<Brand, CategoryProductsViewHolder>(
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
        binding.name.text = current.name
        binding.image.setImageResource(current.image)

//        binding.favConstraint.setOnClickListener {
//            favListener.invoke(
//                current.latitude,
//                current.longitude
//            )
//        }
    }
}

class CategoryProductsViewHolder(private val layout: CategoryProductsLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class CategoryProductsDiffUtil : DiffUtil.ItemCallback<Brand>() {
    override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem == newItem
    }

}