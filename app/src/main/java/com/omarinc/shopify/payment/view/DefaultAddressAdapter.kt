package com.omarinc.shopify.payment.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CustomerAddress

class DefaultAddressAdapter(
    private val addresses: List<CustomerAddress?>,

    ) : RecyclerView.Adapter<DefaultAddressAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.default_address_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return addresses.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = addresses[position]

        holder.customerName.text = "${item?.firstName} ${item?.lastName}"
        holder.customerAddress.text = item?.address1
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val customerName: TextView = itemView.findViewById(R.id.customer_name_text_view)
        val customerAddress: TextView = itemView.findViewById(R.id.address_text_view)
    }
}