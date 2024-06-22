package com.omarinc.shopify.shoppingcart.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.utilities.Helper

class ShoppingCartAdapter(
    private val context: Context,
    private var items: List<CartProduct>,
    private val onRemoveItem: (String) -> Unit
) : RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {

    private var convertedPrices: MutableMap<String, Double> = mutableMapOf()
    private var currencyUnit: String = "EGP"

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
        val convertedPrice = convertedPrices[item.id] ?: item.variantPrice.toDouble()

        holder.productName.text = item.productTitle
        holder.productPrice.text = String.format("%.2f %s", convertedPrice, currencyUnit)
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

    fun updateItems(newItems: List<CartProduct>) {
        val diffCallback = CartProductDiffCallback(this.items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateCurrentCurrency(rate: Double, unit: String) {
        currencyUnit = unit
        val newPrices = items.map { product ->
            product.id to product.variantPrice.toDouble() * rate
        }.toMap()
        convertedPrices.putAll(newPrices)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.shopping_cart_product_name_text_view)
        val productPrice: TextView = itemView.findViewById(R.id.shopping_cart_product_price_text_view)
        val removeImageView: ImageView = itemView.findViewById(R.id.shopping_cart_delete_icon)
        val productImage: ImageView = itemView.findViewById(R.id.shopping_cart_product_image_view)
    }

}

class CartProductDiffCallback(
    private val oldList: List<CartProduct>,
    private val newList: List<CartProduct>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
