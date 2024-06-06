package com.omarinc.shopify.shoppingcart.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CartProduct

class ShoppingCartAdapter(
    private val items:List<CartProduct>
):RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shopping_cart_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]
        holder.productName.text = item.productTitle
        holder.productPrice.text = item.variantPrice
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

        val productName:TextView=itemView.findViewById(R.id.shopping_cart_product_name_text_view)
        val productPrice:TextView=itemView.findViewById(R.id.shopping_cart_product_price_text_view)

    }
}