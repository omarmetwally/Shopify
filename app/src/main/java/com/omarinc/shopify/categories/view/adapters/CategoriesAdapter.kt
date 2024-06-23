package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.CategoriesLayoutBinding
import com.omarinc.shopify.models.Brand


class CategoriesAdapter(
    val context: Context,
   // private val listener: (weather: Favourites)->Unit,
    //private val favListener: (latitude:Double, longitude:Double)->Unit
) : ListAdapter<Brand, CategoriesViewHolder>(
    CategoriesDiffUtil()
) {

    private lateinit var binding: CategoriesLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
         binding = CategoriesLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
       // binding.favCard.setCardBackgroundColor( setCardViewBackground(context))
        val current = getItem(position)

        binding.categoryName.text = current.name
        binding.categoryImage.setImageResource(current.image)

//        binding.favConstraint.setOnClickListener {
//            favListener.invoke(
//                current.latitude,
//                current.longitude
//            )
//        }
    }
}

class CategoriesViewHolder(private val layout: CategoriesLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class CategoriesDiffUtil : DiffUtil.ItemCallback<Brand>() {
    override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
        return oldItem == newItem
    }

}