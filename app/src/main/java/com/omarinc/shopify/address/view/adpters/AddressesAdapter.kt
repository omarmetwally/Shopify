package com.omarinc.shopify.address.view.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.utilities.Helper

class AddressesAdapter(
    private val context: Context,
    private val addresses: List<CustomerAddress?>,
    private val onRemoveItem: (String) -> Unit

) : RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.address_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = addresses[position]
        holder.customerName.text = "${item?.firstName} ${item?.lastName}"
        holder.customerAddress.text = item?.address1
        holder.deleteIcon.setOnClickListener {
            Helper.showAlertDialog(
                context = context,
                title = context.getString(R.string.delete_address_item),
                message = context.getString(R.string.are_you_sure_to_address_this_item),
                positiveButtonText = context.getString(R.string.yes),
                positiveButtonAction = {
                    onRemoveItem(item?.id.toString())
                },
                negativeButtonText = context.getString(R.string.no)
            )


        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val customerName: TextView = itemView.findViewById(R.id.customer_name_text_view)
        val customerAddress: TextView = itemView.findViewById(R.id.address_text_view)
        val deleteIcon:ImageView = itemView.findViewById(R.id.address_delete_icon)
    }
}