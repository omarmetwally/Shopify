package com.omarinc.shopify.home.view

import android.os.Bundle
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
import com.example.weatherforecastapplication.favouritesFeature.view.AdsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.Brand
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.sharedpreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var brandsManager: GridLayoutManager
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var adsAdapter: AdsAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = HomeViewModel.HomeViewModelFactory(ShopifyRepositoryImpl(ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext())))

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        viewModel.getBrands()
        viewModel.getProductsByBrandId("gid://shopify/Collection/308804419763")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
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
        brandsAdapter = BrandsAdapter(
            requireContext(),
            {

            }
        )

        brandsManager = GridLayoutManager(requireContext(),2)
        brandsManager.orientation = LinearLayoutManager.VERTICAL
        binding.brandsRV.layoutManager = brandsManager
        binding.brandsRV.adapter = brandsAdapter

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

        //brandsAdapter.submitList(dummyBrands)
    }
}