package com.omarinc.shopify.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.CategoriesAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCategoriesBinding
import com.omarinc.shopify.home.viewmodel.CategoriesViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch


class CategoriesFragment : Fragment() {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoriesManager: LinearLayoutManager
    private lateinit var categoriessAdapter: CategoriesAdapter
    private lateinit var viewModel: CategoriesViewModel
    private var isFilter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    private fun setupViewModel() {
        val factory = CategoriesViewModel.CategoriesViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),

                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            )


        )

        viewModel = ViewModelProvider(this, factory).get(CategoriesViewModel::class.java)

        viewModel.getCollectionByHandle("men")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnClickListener {
            findNavController().navigate(R.id.action_categoriesFragment_to_searchFragment)

        }
        setupCategoriesAdapter()

        setupFilterView()

        setupCategoriesView()
        setupSeekBar()
    }

    private fun setupCategoriesAdapter() {
        categoriessAdapter = CategoriesAdapter(
            requireContext(),
        )

        categoriesManager = LinearLayoutManager(requireContext())
        categoriesManager.orientation = LinearLayoutManager.VERTICAL
    }

    private fun setupFilterView() {
        binding.filterView.setOnClickListener {
            if (isFilter) {
                binding.priceSeekBar.visibility = View.GONE
                binding.seekBarValueText.visibility = View.GONE
                isFilter = !isFilter
            } else {
                binding.priceSeekBar.visibility = View.VISIBLE
                binding.seekBarValueText.visibility = View.VISIBLE
                isFilter = !isFilter
            }
        }
    }


    private fun setupCategoriesView() {
        binding.men.setOnClickListener {
            viewModel.getCollectionByHandle("men")
            binding.menText.setTextColor(resources.getColor(R.color.black))
            binding.womenText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.kidText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.saleText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.menDivider.visibility = View.VISIBLE
            binding.womenDivider.visibility = View.GONE
            binding.kidDivider.visibility = View.GONE
            binding.saleDivider.visibility = View.GONE
        }
        binding.women.setOnClickListener {
            viewModel.getCollectionByHandle("women")
            binding.menText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.womenText.setTextColor(resources.getColor(R.color.black))
            binding.kidText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.saleText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.menDivider.visibility = View.GONE
            binding.womenDivider.visibility = View.VISIBLE
            binding.kidDivider.visibility = View.GONE
            binding.saleDivider.visibility = View.GONE
        }
        binding.kid.setOnClickListener {
            viewModel.getCollectionByHandle("kid")
            binding.menText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.womenText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.kidText.setTextColor(resources.getColor(R.color.black))
            binding.saleText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.menDivider.visibility = View.GONE
            binding.womenDivider.visibility = View.GONE
            binding.kidDivider.visibility = View.VISIBLE
            binding.saleDivider.visibility = View.GONE
        }
        binding.sale.setOnClickListener {
            viewModel.getCollectionByHandle("sale")
            binding.menText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.womenText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.kidText.setTextColor(resources.getColor(R.color.dark_grey))
            binding.saleText.setTextColor(resources.getColor(R.color.black))
            binding.menDivider.visibility = View.GONE
            binding.womenDivider.visibility = View.GONE
            binding.kidDivider.visibility = View.GONE
            binding.saleDivider.visibility = View.VISIBLE
        }

    }


    private fun setupSeekBar() {
        binding.priceSeekBar.max = 1000
        binding.priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.maxPrice.value = progress
                binding.seekBarValueText.text = "Max Price: $progress"
                collectFilteredProducts()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun collectFilteredProducts() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.collectionApiState.collect { results ->
                        if (results is ApiState.Success)
                            viewModel.filterProducts(results.response.products)
                    }
                }
            }
        }
    }
}

