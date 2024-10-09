package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.OrderLayoutBinding
import com.omarinc.shopify.models.Order


class OrdersAdapter(
    val context: Context,
    private val listener: (index :Int)->Unit,
) : ListAdapter<Order, OrdersViewHolder>(
    OrdersDiffUtil()
) {

    private lateinit var binding: OrderLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
         binding = OrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val current = getItem(position)

        binding.totalPrice.text = "${current.totalPriceAmount} EGP - ${current.subTotalPriceAmount} items"
        binding.dateCreated.text = current.processedAt
        Glide.with(context).load(current.products[0].imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.productImage1)

        Glide.with(context).load(current.products[0].imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.productImage2)

        binding.orderConstrainLayout.setOnClickListener {
            listener.invoke(position)
            Log.i("TAG", "onBindViewHolder: id current "+current.id)

        }
        binding.details.setOnClickListener {
            listener.invoke(position)
            Log.i("TAG", "onBindViewHolder: id current "+current.id)

        }
    }
}

class OrdersViewHolder(val layout: OrderLayoutBinding) : RecyclerView.ViewHolder(layout.root)
class OrdersDiffUtil : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }

}