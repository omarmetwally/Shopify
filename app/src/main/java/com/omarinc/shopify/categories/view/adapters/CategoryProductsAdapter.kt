package com.omarinc.shopify.categories.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.CategoryProductsLayoutBinding
import com.omarinc.shopify.models.Product

class CategoryProductsAdapter(
    val context: Context,
) : ListAdapter<Product, CategoryProductsViewHolder>(
    CategoryProductsDiffUtil()
) {

    private lateinit var binding: CategoryProductsLayoutBinding
    private var convertedPrices: MutableMap<String, Double> = mutableMapOf()
    private var currencyUnit: String = "USD"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductsViewHolder {
        binding = CategoryProductsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductsViewHolder, position: Int) {
        val current = getItem(position)
        val convertedPrice = convertedPrices[current.id] ?: current.price.toDouble()

        holder.bind(current, convertedPrice, currencyUnit)
    }

    fun updateCurrentCurrency(rate: Double, unit: String) {
        currencyUnit = unit
        val newPrices = currentList.map { product ->
            product.id to product.price.toDouble() * rate
        }.toMap()
        convertedPrices.putAll(newPrices)
        notifyDataSetChanged()
    }
}

class CategoryProductsViewHolder(private val binding: CategoryProductsLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(product: Product, convertedPrice: Double, currencyUnit: String) {
        binding.name.text = product.title
        binding.price.text = String.format("%.2f %s", convertedPrice, currencyUnit)
        Glide.with(binding.root.context).load(product.imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.image)
    }
}

class CategoryProductsDiffUtil : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}
