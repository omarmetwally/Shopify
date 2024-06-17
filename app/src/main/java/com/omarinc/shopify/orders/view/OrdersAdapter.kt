package com.omarinc.shopify.orders.view

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
    private val listener: (index: Int) -> Unit,
) : ListAdapter<Order, OrdersViewHolder>(
    OrdersDiffUtil()
) {

    private lateinit var binding: OrderLayoutBinding
    private var convertedPrices: MutableMap<String, Double> = mutableMapOf()
    private var currencyUnit: String = "EGP"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        binding = OrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val current = getItem(position)
        val convertedTotalPrice = convertedPrices["totalPrice${current.id}"] ?: current.totalPriceAmount.toDouble()
        val convertedSubTotalPrice = convertedPrices["subTotalPrice${current.id}"] ?: current.subTotalPriceAmount.toDouble()

        binding.totalPrice.text = String.format("%.2f %s - %d items", convertedTotalPrice, currencyUnit, current.products.size)
        binding.dateCreated.text = current.processedAt

        Glide.with(context).load(current.products[0].imageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(binding.productImage1)

        if (current.products.size > 1) {
            Glide.with(context).load(current.products[1].imageUrl)
                .apply(
                    RequestOptions().override(200, 200)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_background)
                )
                .into(binding.productImage2)
        } else {
            binding.productImage2.setImageResource(R.drawable.ic_launcher_foreground) // Placeholder image
        }

        binding.orderConstrainLayout.setOnClickListener {
            listener.invoke(position)
            Log.i("TAG", "onBindViewHolder: id current " + current.id)
        }

        binding.details.setOnClickListener {
            listener.invoke(position)
            Log.i("TAG", "onBindViewHolder: id current " + current.id)
        }
    }

    fun updateCurrentCurrency(rate: Double, unit: String) {
        currencyUnit = unit
        currentList.forEach { order ->
            convertedPrices["totalPrice${order.id}"] = order.totalPriceAmount.toDouble() * rate
            convertedPrices["subTotalPrice${order.id}"] = order.subTotalPriceAmount.toDouble() * rate
        }
        notifyDataSetChanged()
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
