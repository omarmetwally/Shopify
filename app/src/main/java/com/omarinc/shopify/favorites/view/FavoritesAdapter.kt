package com.omarinc.shopify.favorites.view

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
import com.omarinc.shopify.favorites.model.FavoriteItemFirebase

class FavoritesAdapter(
    private val context: Context,
    private val listener: (id: String) -> Unit
) : ListAdapter<FavoriteItemFirebase, FavoritesAdapter.FavoriteViewHolder>(FavoritesDiffUtil()) {

    private var lastPosition = -1
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val current = getItem(position)
        holder.bind(current, context)

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
            listener.invoke(current.productId)
        }
    }

    class FavoriteViewHolder(private val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favoriteItem: FavoriteItemFirebase, context: Context) {
            binding.tvProductName.text = favoriteItem.productName
            binding.tvProductPrice.text = "${favoriteItem.productPrice} USD"
            Glide.with(context).load(favoriteItem.productImage)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_background))
                .into(binding.ivProductImage)
        }
    }

    class FavoritesDiffUtil : DiffUtil.ItemCallback<FavoriteItemFirebase>() {
        override fun areItemsTheSame(oldItem: FavoriteItemFirebase, newItem: FavoriteItemFirebase): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: FavoriteItemFirebase, newItem: FavoriteItemFirebase): Boolean {
            return oldItem == newItem
        }
    }
}
