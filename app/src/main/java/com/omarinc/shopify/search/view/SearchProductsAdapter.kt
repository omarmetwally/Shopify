package com.omarinc.shopify.search.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.ItemFavoriteBinding
import com.omarinc.shopify.productdetails.model.Products

class SearchProductsAdapter(
    private val context: Context,
    private val listener: (id: String) -> Unit
) : ListAdapter<Products, SearchProductsAdapter.ProductViewHolder>(ProductDiffUtil()) {

    private var lastPosition = -1
    private val handler = Handler(Looper.getMainLooper())

    private var convertedPrices: MutableMap<String, Double> = mutableMapOf()
    private var currencyUnit: String = "USD"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val current = getItem(position)
        val convertedPrice = convertedPrices[current.id] ?: current.price

        holder.bind(current, convertedPrice, context, currencyUnit)

        if (position > lastPosition) {
            holder.itemView.visibility = View.INVISIBLE
            handler.postDelayed({
                val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_in_animation)
                holder.itemView.startAnimation(animation)
                holder.itemView.visibility = View.VISIBLE
            }, (position * 170).toLong())
            lastPosition = position
        }

        holder.itemView.setOnClickListener {
            listener.invoke(current.id)
        }
    }

    fun updateCurrentCurrency(rate: Double, unit: String) {
        currencyUnit = unit
        currentList.forEach { product ->
            convertedPrices[product.id] = product.price * rate
        }
        notifyDataSetChanged()
    }

    class ProductViewHolder(private val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Products, convertedPrice: Double, context: Context, currencyUnit: String) {
            binding.tvProductName.text = product.title
            binding.tvProductPrice.text = String.format("%.2f %s", convertedPrice, currencyUnit)
            Glide.with(context).load(product.imageUrl)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_background))
                .into(binding.ivProductImage)
        }
    }

    class ProductDiffUtil : DiffUtil.ItemCallback<Products>() {
        override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
            return oldItem == newItem
        }
    }
}
