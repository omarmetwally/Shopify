package com.omarinc.shopify.shoppingcart.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.shopify.databinding.FragmentShoppingCartBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModel
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModelFactory
import kotlinx.coroutines.launch

class ShoppingCartFragment : Fragment() {

    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var viewModel: ShoppingCartViewModel

    companion object {
        const val TAG = "ShoppingCartFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        getShoppingCartItems()
        setListeners()
    }

    private fun setListeners() {

    }

    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory = ShoppingCartViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ShoppingCartViewModel::class.java)
    }

    private fun getShoppingCartItems() {
        viewModel.getShoppingCartItems(viewModel.readCartId())
        Log.i(TAG, "getShoppingCartItems: ${viewModel.readCartId()}")
        lifecycleScope.launch {
            viewModel.cartItems.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "onViewCreated: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "onViewCreated: Loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "onViewCreated: ${result.response.size}")
                        setupRecyclerView(result.response)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(items: List<CartProduct>) {

        val adapter = ShoppingCartAdapter(items) { itemId ->
            val cartId = viewModel.readCartId()
            Log.i(TAG, "setupRecyclerView: $cartId")
            removeItemFromCart(cartId, itemId)
        }

        binding.shoppingCartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            this.adapter = adapter
        }
        adapter.notifyDataSetChanged()

    }

    private fun removeItemFromCart(cartId: String, lineId: String) {

        viewModel.removeProductFromCart(cartId, lineId)

        lifecycleScope.launch {
            viewModel.cartItemRemove.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "removeItemFromCart: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "removeItemFromCart: Loading")
                    is ApiState.Success -> {
                        getShoppingCartItems()

                    }
                }
            }
        }
    }

}
