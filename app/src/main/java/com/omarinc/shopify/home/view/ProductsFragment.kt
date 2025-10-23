package com.omarinc.shopify.home.view

import android.R
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.omarinc.shopify.home.view.adapters.ProductsAdapter
import com.omarinc.shopify.databinding.FragmentProductsBinding
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class ProductsFragment : Fragment() {

    companion object{
        private const val TAG = "ProductsFragment"
    }

    private lateinit var binding: FragmentProductsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var suggestionsAdapter: CursorAdapter
    private var isFilter = false

    private val searchQuery = MutableStateFlow("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupSeekBar()
        setupFilterView()
        setupProductsAdapter()
        setupSuggestionsAdapter()
        setupSearchView()
        collectSearchQuery()
        collectProducts()
        collectFilteredProducts()
    }
    private fun collectProducts() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {
                            binding.productsShimmer.startShimmer()
                        }

                        is ApiState.Success -> {
                            Log.i(TAG, "collectProducts: ")
                            binding.productsShimmer.stopShimmer()
                            binding.productsShimmer.visibility = View.GONE
                            productsAdapter.submitList(result.response)
                            val currencyUnit = SharedPreferencesImpl.getInstance(requireContext())
                            if (!currencyUnit.readCurrencyUnitFromSharedPreferences(Constants.CURRENCY_UNIT).equals("EGP")){
                                getCurrentCurrency()
                            }
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }
    }
    private fun setupProductsAdapter(){
        productsAdapter = ProductsAdapter(requireContext()) { productId ->
            val action =
                ProductsFragmentDirections.actionProductsFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        productsManager = GridLayoutManager(requireContext(), 2)
        productsManager.orientation = LinearLayoutManager.VERTICAL
        binding.productsRV.layoutManager = productsManager
        binding.productsRV.adapter = productsAdapter


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
    private fun setupSeekBar() {
        val currencyUnit = SharedPreferencesImpl.getInstance(requireContext())
        binding.priceSeekBar.max = 10000
        binding.priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.maxPrice.value = progress
                binding.seekBarValueText.text = "Max Price: $progress ${currencyUnit.readCurrencyUnitFromSharedPreferences(
                    Constants.CURRENCY_UNIT)}"
                lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        launch {
                            viewModel.productsState.collect { results ->
                                if (results is ApiState.Success) {
                                    viewModel.filterProducts(results.response)
                                }
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun collectFilteredProducts() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredProductsState.collect { result ->
                    when (result) {
                        is ApiState.Loading -> {
                            binding.productsShimmer.startShimmer()
                        }

                        is ApiState.Success -> {
                            Log.i(TAG, "collectProducts: ")
                            binding.productsShimmer.stopShimmer()
                            binding.productsShimmer.visibility = View.GONE
                            productsAdapter.submitList(result.response)
                            val currencyUnit = SharedPreferencesImpl.getInstance(requireContext())
                            if (!currencyUnit.readCurrencyUnitFromSharedPreferences(Constants.CURRENCY_UNIT).equals("EGP")){
                                getCurrentCurrency()
                            }
                        }

                        is ApiState.Failure -> {

                        }
                    }
                }
            }
        }
    }
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery.value = newText ?: ""
                updateSuggestions(getSuggestions(newText ?: ""))
                return true
            }
        })

        binding.searchView.suggestionsAdapter = suggestionsAdapter
        binding.searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = suggestionsAdapter.cursor
                cursor.moveToPosition(position)
                val suggestion = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"))
                binding.searchView.setQuery(suggestion, true)
                return true
            }
        })
    }
    private fun collectSearchQuery() {
        lifecycleScope.launch {
            searchQuery.debounce(150)
                .distinctUntilChanged()
                .collect { query ->
                    filterProducts(query)
                }
        }
    }
    private fun filterProducts(query: String) {
        viewModel.productsState.value.let { state ->
            if (state is ApiState.Success) {
                val filteredList = state.response.filter { product ->
                    product.title.contains(query, ignoreCase = true)
                }
                productsAdapter.submitList(filteredList)
            }
        }
    }
    private fun setupSuggestionsAdapter() {
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        suggestionsAdapter = SimpleCursorAdapter(
            requireContext(),
            R.layout.simple_list_item_1,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
    }
    private fun updateSuggestions(suggestions: List<String>) {
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, "suggestion"))
        suggestions.forEachIndexed { index, suggestion ->
            cursor.addRow(arrayOf(index, suggestion))
        }
        suggestionsAdapter.changeCursor(cursor)
    }
    private fun getSuggestions(query: String): List<String> {
        viewModel.productsState.value.let { state ->
            if (state is ApiState.Success) {
                return state.response.map { it.title }
                    .filter { it.contains(query, ignoreCase = true) }
            }
        }
        return emptyList()
    }
    private fun setupViewModel(){
        val factory = HomeViewModel.HomeViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()

            ),
            FirebaseRepository.getInstance()
        )

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        val id = arguments?.getString("id")
        viewModel.getProductsByBrandId(id ?: "gid://shopify/Collection/308804419763")
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
                    is ApiState.Failure -> Log.i(TAG, "getCurrentCurrency: ${requiredCurrency.msg}")
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