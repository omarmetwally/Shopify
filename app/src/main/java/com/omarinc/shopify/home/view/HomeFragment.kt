package com.omarinc.shopify.home.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.AdsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var brandsManager: GridLayoutManager
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var adsAdapter: AdsAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //ACCESSORIES T-SHIRTS SHOES

        val factory = HomeViewModel.HomeViewModelFactory(ShopifyRepositoryImpl(ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance()))

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        viewModel.getBrands()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        adsAdapter = AdsAdapter(requireContext())

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            brandsAdapter.submitList(result.response)
                        }
                        is ApiState.Failure ->{

                        }
                    }
                }
            }
        }

        binding.adsVP.adapter = adsAdapter
        val images = listOf(
            R.drawable.shoe,
            R.drawable.discount,
            R.drawable.shoe,
            R.drawable.discount,
        )

        adsAdapter.submitList(images)
        binding.adsVP.setPageTransformer(ZoomOutPageTransformer())

        val onBrandClick = { id : String ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductsFragment(
                    id
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        brandsAdapter = BrandsAdapter(
            requireContext(),
            {
                Log.i("TAG", "onViewCreated: it id "+it)
                val action = HomeFragmentDirections
                    .actionHomeFragmentToProductsFragment(
                        it
                    )
                Navigation.findNavController(requireView()).navigate(action)
            }
        )

        brandsManager = GridLayoutManager(requireContext(),2)
        brandsManager.orientation = LinearLayoutManager.VERTICAL
        binding.brandsRV.layoutManager = brandsManager
        binding.brandsRV.adapter = brandsAdapter

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