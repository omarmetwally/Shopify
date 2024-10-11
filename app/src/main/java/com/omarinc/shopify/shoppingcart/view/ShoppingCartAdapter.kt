package com.omarinc.shopify.shoppingcart.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.utilities.Helper

class ShoppingCartAdapter(
    private val context: Context,
    private val items: List<CartProduct>,
    private val onRemoveItem: (String) -> Unit
) : RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {

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
        Glide.with(context).load(item.productImageUrl)
            .apply(
                RequestOptions().override(200, 200)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_background)
            )
            .into(holder.productImage)

        holder.removeImageView.setOnClickListener {
            Helper.showAlertDialog(
                context = context,
                title = context.getString(R.string.delete_cart_item),
                message = context.getString(R.string.are_you_sure_to_delete_this_item),
                positiveButtonText = context.getString(R.string.yes),
                positiveButtonAction = {
                    onRemoveItem(item.id)
                },
                negativeButtonText = context.getString(R.string.no)
            )
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.shopping_cart_product_name_text_view)
        val productPrice: TextView =
            itemView.findViewById(R.id.shopping_cart_product_price_text_view)
        val removeImageView: ImageView = itemView.findViewById(R.id.shopping_cart_delete_icon)
        val productImage: ImageView =
            itemView.findViewById(R.id.shopping_cart_product_image_view)
    }
}
