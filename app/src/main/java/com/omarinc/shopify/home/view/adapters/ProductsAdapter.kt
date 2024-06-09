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
import com.omarinc.shopify.databinding.ProductLayoutBinding
import com.omarinc.shopify.models.Product


class ProductsAdapter(
    val context: Context,
    private val listener: (id: String)->Unit,
) : ListAdapter<Product, ProductsViewHolder>(
    ProductsDiffUtil()
) {

    private lateinit var binding: ProductLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        binding = ProductLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val current = getItem(position)

        binding.brandName.text = current.title
        Glide.with(context).load(current.imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.brandImage)
        binding.brandConstrainLayout.setOnClickListener {
            listener.invoke(current.id)

        }
    }
}

class ProductsViewHolder(val layout: ProductLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class ProductsDiffUtil : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }

}