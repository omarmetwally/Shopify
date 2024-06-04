package com.omarinc.shopify.categories.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.CategoryProductsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCategoryProductsBinding
import com.omarinc.shopify.home.viewmodel.CategoriesViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.Brand
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.view.ProductDetailsFragment
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class CategoryProductsFragment : Fragment() {

    private lateinit var binding: FragmentCategoryProductsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: CategoryProductsAdapter
    private var isFabOpen = false
    private lateinit var fabOpenAnim: Animation
    private lateinit var fabCloseAnim: Animation
    private lateinit var rotateForwardAnim: Animation
    private lateinit var rotateBackwardAnim: Animation
    private lateinit var viewModel: CategoriesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = CategoriesViewModel.CategoriesViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance())
        )

        viewModel = ViewModelProvider(requireParentFragment(), factory).get(CategoriesViewModel::class.java)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryProductsBinding.inflate(layoutInflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productsAdapter = CategoryProductsAdapter(
            requireContext(),
        )

        productsManager = GridLayoutManager(requireContext(),2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.categoryProductsRV.layoutManager = productsManager
        binding.categoryProductsRV.adapter = productsAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            productsAdapter.submitList(result.response)
                        }
                        is ApiState.Failure ->{
                            Log.i("TAG", "onViewCreated: error "+result.msg)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.collectionApiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            productsAdapter.submitList(result.response.products)
                        }
                        is ApiState.Failure ->{
                            Log.i("TAG", "onViewCreated: error "+result.msg)
                        }
                    }
                }
            }
        }

        binding.fab1.setOnClickListener {
            viewModel.getProductsByType("SHOES")
        }

        binding.fab2.setOnClickListener {
            viewModel.getProductsByType("T-SHIRTS")
        }
        binding.fab3.setOnClickListener {
            viewModel.getProductsByType("ACCESSORIES")
        }

        fabOpenAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_open)
        fabCloseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_close)
        rotateForwardAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_forward)
        rotateBackwardAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_backword)

        binding.mainFab.setOnClickListener { toggleFab() }
    }


    private fun getCurrentCurrency(){

        viewModel.getRequiredCurrency()
        lifecycleScope.launch {
            viewModel.requiredCurrency.collect {result->

                when(result){
                    is ApiState.Failure -> Log.i(ProductDetailsFragment.TAG, "getCurrentCurrency: ${result.msg}")
                    ApiState.Loading -> Log.i(ProductDetailsFragment.TAG, "getCurrentCurrency: Loading")
                    is ApiState.Success -> Log.i(ProductDetailsFragment.TAG, "getCurrentCurrency: ${result.response.data.values}")
                }

            }
        }
    }
    private fun toggleFab() {
        if (isFabOpen) {
            binding.mainFab.startAnimation(rotateBackwardAnim)
            binding.fab1.startAnimation(fabCloseAnim)
            binding.fab2.startAnimation(fabCloseAnim)
            binding.fab3.startAnimation(fabCloseAnim)
            binding.fab1.visibility = View.GONE
            binding.fab2.visibility = View.GONE
            binding.fab3.visibility = View.GONE
            binding.fab1.isClickable = false
            binding.fab2.isClickable = false
            binding.fab3.isClickable = false
            isFabOpen = false
        } else {
            binding.mainFab.startAnimation(rotateForwardAnim)
            binding.fab1.startAnimation(fabOpenAnim)
            binding.fab2.startAnimation(fabOpenAnim)
            binding.fab3.startAnimation(fabOpenAnim)
            binding.fab1.visibility = View.VISIBLE
            binding.fab2.visibility = View.VISIBLE
            binding.fab3.visibility = View.VISIBLE
            binding.fab1.isClickable = true
            binding.fab2.isClickable = true
            binding.fab3.isClickable = true
            isFabOpen = true
        }
    }
}