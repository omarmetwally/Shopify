package com.omarinc.shopify.home.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.ProductsAdapter
import com.omarinc.shopify.databinding.FragmentProductsBinding
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.network.ApiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val factory = HomeViewModel.HomeViewModelFactory(
//            ShopifyRepositoryImpl(
//                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
//            SharedPreferencesImpl.getInstance(requireContext()),
//            CurrencyRemoteDataSourceImpl.getInstance())
//        )

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val id =  arguments?.getString("id")
        Log.i("TAG", "onCreate: id"+id)

        viewModel.getProductsByBrandId(id?:"gid://shopify/Collection/308804419763")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsAdapter = ProductsAdapter(
            requireContext(),
            {}
        )


        productsManager = GridLayoutManager(requireContext(),2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.productsRV.layoutManager = productsManager
        binding.productsRV.adapter = productsAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.productsApiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            productsAdapter.submitList(result.response)
                        }
                        is ApiState.Failure ->{

                        }
                    }
                }
            }
        }


    }
}