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
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCategoryProductsBinding
import com.omarinc.shopify.categories.viewmodel.CategoriesViewModel
import com.omarinc.shopify.home.view.HomeFragment
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.view.ProductDetailsFragment
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CategoryProductsFragment : Fragment() {

    companion object {
        const val TAG = "CategoryProductsFragment"
    }

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

        setUpViewModel()

    }

    private fun setUpViewModel() {
        val factory = CategoriesViewModel.CategoriesViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            )
        )

        viewModel =
            ViewModelProvider(requireParentFragment(), factory).get(CategoriesViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryProductsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpProductsAdapter()
        collectProductsByHandle()


        binding.fab1.setOnClickListener {
            viewModel.getProductsByType("SHOES")
            toggleFab()
            binding.mainFab.setImageResource(R.drawable.shoes)
        }

        binding.fab2.setOnClickListener {
            viewModel.getProductsByType("T-SHIRTS")
            toggleFab()
            binding.mainFab.setImageResource(R.drawable.shirt)
        }
        binding.fab3.setOnClickListener {
            viewModel.getProductsByType("ACCESSORIES")
            toggleFab()
            binding.mainFab.setImageResource(R.drawable.accessories)
        }

        setUpFabAnimation()
        collectProductsBySubCategories()
        binding.mainFab.setOnClickListener { toggleFab() }
    }

    private fun collectProductsBySubCategories() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            productsAdapter.submitList(result.response)
                            Log.i(TAG, "collectProductsBySubCategories: ")
                            getCurrentCurrency()
                        }

                        is ApiState.Failure -> {
                            Log.i("TAG", "onViewCreated: error " + result.msg)
                        }
                    }
                }
            }
        }
    }

    private fun collectProductsByHandle() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.collectionApiState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {

                        }

                        is ApiState.Success -> {
                            productsAdapter.submitList(result.response.products)
                            getCurrentCurrency()
                        }

                        is ApiState.Failure -> {
                            Log.i("TAG", "onViewCreated: error " + result.msg)
                        }
                    }
                }
            }
        }
    }

    private fun setUpFabAnimation() {
        fabOpenAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_open)
        fabCloseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_close)
        rotateForwardAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_forward)
        rotateBackwardAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_backword)

    }

    private fun setUpProductsAdapter() {
        productsAdapter = CategoryProductsAdapter(
            requireContext(),
        )

        productsManager = GridLayoutManager(requireContext(), 2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.categoryProductsRV.layoutManager = productsManager
        binding.categoryProductsRV.adapter = productsAdapter

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
            binding.mainFab.setImageResource(R.drawable.close)
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

    private fun getCurrentCurrency() {
        viewModel.getCurrencyUnit()
        viewModel.getRequiredCurrency()

        lifecycleScope.launch {
            combine(
                viewModel.currencyUnit,
                viewModel.requiredCurrency
            ) { currencyUnit, requiredCurrency ->
                Pair(currencyUnit, requiredCurrency)
            }.collect { (currencyUnit, requiredCurrency) ->
                Log.i(TAG, "getCurrentCurrency 000: $currencyUnit")
                when (requiredCurrency) {
                    is ApiState.Failure -> Log.i(
                        TAG,
                        "getCurrentCurrency: ${requiredCurrency.msg}"
                    )

                    ApiState.Loading -> Log.i(TAG, "getCurrentCurrency: Loading")
                    is ApiState.Success -> {
                        Log.i(
                            TAG,
                            "getCurrentCurrency: ${requiredCurrency.response.data[currencyUnit]?.code}"
                        )
                        requiredCurrency.response.data[currencyUnit]?.let { currency ->
                            Log.i(TAG, "getCurrentCurrency: ${currency.value}")
                            productsAdapter.updateCurrentCurrency(currency.value, currency.code)
                        }
                    }
                }
            }
        }
    }

}