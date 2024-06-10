package com.omarinc.shopify.search.view

import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omarinc.shopify.databinding.FragmentSearchBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.productdetails.model.Products
import com.omarinc.shopify.search.viewmodel.SearchViewModel
import com.omarinc.shopify.search.viewmodel.SearchViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: FragmentSearchBinding
    private lateinit var productsAdapter: SearchProductsAdapter
    private lateinit var suggestionsAdapter: CursorAdapter
    private val searchQuery = MutableStateFlow("")
    private val maxPrice = MutableStateFlow<Int>(10000)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = SearchViewModelFactory(
            ShopifyRepositoryImpl(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            )
        )
        viewModel = ViewModelProvider(this, factory).get(SearchViewModel::class.java)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        setupRecyclerView()
        setupSearchView()
        setupSeekBar()
        setupFilterButton()
        collectSearchQuery()
        collectSearchResults()
        collectMaxPrice()
    }

    private fun setupRecyclerView() {
        productsAdapter = SearchProductsAdapter(requireContext()) { productId ->
            val action = SearchFragmentDirections.actionSearchFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        binding.productsRV.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.productsRV.adapter = productsAdapter
    }

    private fun setupSearchView() {
        setupSuggestionsAdapter()
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
                    viewModel.searchProducts(query)
                }
        }
    }

    private fun collectSearchResults() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { results ->
                        filterAndDisplayProducts(results)
                    }
                }
            }
        }
    }

    private fun filterAndDisplayProducts(products: List<Products>) {
        val filteredResults = products.filter {
            val price = it.price as? Double ?: (it.price as? String)?.toDoubleOrNull() ?: Double.MAX_VALUE
            price <= maxPrice.value
        }
        productsAdapter.submitList(filteredResults)
        updateSuggestions(filteredResults.map { it.title })
    }

    private fun setupSeekBar() {
        binding.priceSeekBar.max = 1000
        binding.priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxPrice.value = progress
                binding.seekBarValueText.text = "Max Price: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupFilterButton() {
        binding.filterView.setOnClickListener {
            val isVisible = binding.priceSeekBar.visibility == View.VISIBLE
            binding.priceSeekBar.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.seekBarValueText.visibility = if (isVisible) View.GONE else View.VISIBLE
        }
    }

    private fun collectMaxPrice() {
        lifecycleScope.launch {
            maxPrice.collect {
                filterAndDisplayProducts(viewModel.searchResults.value)
            }
        }
    }

    private fun setupSuggestionsAdapter() {
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        suggestionsAdapter = SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
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
        viewModel.searchResults.value.let { results ->
            return results.filter { it.title.contains(query, ignoreCase = true) }
                .map { it.title }
        }
    }
}
