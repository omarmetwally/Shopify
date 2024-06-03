package com.example.weatherforecastapplication.favouritesFeature.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.OrderLayoutBinding
import com.omarinc.shopify.models.Order


class OrdersAdapter(
    val context: Context,
   // private val listener: (id: String)->Unit,
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
       // binding.favCard.setCardBackgroundColor( setCardViewBackground(context))
        val current = getItem(position)

        binding.orderName.text = current.name
        binding.address.text = current.address
        binding.totalPrice.text = current.totalPriceAmount.toString()
        binding.dateCreated.text = current.canceledAt

        /*binding.orderConstrainLayout.setOnClickListener {
            listener.invoke(current.id)
            Log.i("TAG", "onBindViewHolder: id current "+current.id)

        }*/
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