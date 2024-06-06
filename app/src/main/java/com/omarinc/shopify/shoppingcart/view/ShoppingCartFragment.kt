package com.omarinc.shopify.shoppingcart.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.shopify.databinding.FragmentShoppingCartBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModel
import com.omarinc.shopify.shoppingcart.viewModel.ShoppingCartViewModelFactory
import kotlinx.coroutines.launch


class ShoppingCartFragment : Fragment() {
    
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var viewModel: ShoppingCartViewModel
    
    companion object{
        const val TAG = "ShoppingCartFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupViewModel()
        viewModel.getShoppingCartItems("gid://shopify/Cart/Z2NwLWV1cm9wZS13ZXN0MTowMUhaTkVUS0Q0SzdYMEc5UldCUEUyWFQxUg?key=3e770edab029ad5953788926d1b84a83")
        lifecycleScope.launch {

            viewModel.cartItems.collect{result->

                when(result){
                    is ApiState.Failure -> Log.i(TAG, "onViewCreated: ${result.msg}")
                    ApiState.Loading -> Log.i(TAG, "onViewCreated: Loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "onViewCreated: ${result.response.size}")
                    }
                }

            }
        }
        
    }

    private fun setupViewModel() {


        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val viewModelFactory=ShoppingCartViewModelFactory(repository)
        viewModel = ViewModelProvider(this).get(ShoppingCartViewModel::class.java)

    }


}