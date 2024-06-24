package com.omarinc.shopify.payment.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CustomerAddress

class DefaultAddressAdapter(
    private val addresses: List<CustomerAddress?>,
    private val listener: OnItemClickListener

) : RecyclerView.Adapter<DefaultAddressAdapter.ViewHolder>() {


    companion object{
        private const val TAG ="DefaultAddressAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.default_address_item, parent, false)
        return ViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = addresses[position]

        holder.customerName.text = "${item?.firstName} ${item?.lastName}"
        holder.customerAddress.text = item?.address1
    }

    class ViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        val customerName: TextView = itemView.findViewById(R.id.customer_name_text_view)
        val customerAddress: TextView = itemView.findViewById(R.id.address_text_view)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                    Log.i(TAG, ":${position} ")
                }
            }
        }
    }
}

interface OnItemClickListener {
    fun onItemClick(position: Int)
}
