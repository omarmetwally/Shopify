package com.omarinc.shopify.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.CategoriesAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCategoriesBinding
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.models.Brand


class CategoriesFragment : Fragment() {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoriesManager: LinearLayoutManager
    private lateinit var categoriessAdapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoriesBinding.inflate(layoutInflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriessAdapter = CategoriesAdapter(
            requireContext(),
        )

        categoriesManager = LinearLayoutManager(requireContext())
        categoriesManager.orientation = LinearLayoutManager.VERTICAL
        binding.categoriesRV.layoutManager = categoriesManager
        binding.categoriesRV.adapter = categoriessAdapter

        val dummyBrands = listOf(
            Brand(R.drawable.shoe, "Home page"),
            Brand(R.drawable.shoe, "VANS"),
            Brand(R.drawable.shoe, "PUMA"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),
            Brand(R.drawable.shoe, "PALLADUIM"),

            )

        categoriessAdapter.submitList(dummyBrands)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tool_bar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchFragment -> {
                findNavController().navigate(R.id.searchFragment)
                true
            }
            R.id.favoritesFragment -> {
                findNavController().navigate(R.id.favoritesFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}