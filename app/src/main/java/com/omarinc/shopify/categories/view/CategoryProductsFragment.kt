package com.omarinc.shopify.categories.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.CategoryProductsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCategoryProductsBinding
import com.omarinc.shopify.models.Brand

class CategoryProductsFragment : Fragment() {

    private lateinit var binding: FragmentCategoryProductsBinding
    private lateinit var productsManager: GridLayoutManager
    private lateinit var productsAdapter: CategoryProductsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val dummyBrands = listOf(
            Brand(R.drawable.shoe, "Brand 1"),
            Brand(R.drawable.shoe, "Brand 2"),
            Brand(R.drawable.shoe, "Brand 3"),
            Brand(R.drawable.shoe, "Brand 4"),

            Brand(R.drawable.shoe, "Brand 1"),
            Brand(R.drawable.shoe, "Brand 2"),
            Brand(R.drawable.shoe, "Brand 3"),
            Brand(R.drawable.shoe, "Brand 4"),

            Brand(R.drawable.shoe, "Brand 1"),
            Brand(R.drawable.shoe, "Brand 2"),
            Brand(R.drawable.shoe, "Brand 3"),
            Brand(R.drawable.shoe, "Brand 4"),

            Brand(R.drawable.shoe, "Brand 1"),
            Brand(R.drawable.shoe, "Brand 2"),
            Brand(R.drawable.shoe, "Brand 3"),
            Brand(R.drawable.shoe, "Brand 4"),

            Brand(R.drawable.shoe, "Brand 1"),
            Brand(R.drawable.shoe, "Brand 2"),
            Brand(R.drawable.shoe, "Brand 3"),
            Brand(R.drawable.shoe, "Brand 4"),

            )

        productsAdapter.submitList(dummyBrands)
    }
}