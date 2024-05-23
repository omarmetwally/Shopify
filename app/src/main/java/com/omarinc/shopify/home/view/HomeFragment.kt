package com.omarinc.shopify.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.AdsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.models.Brand

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var brandsManager: GridLayoutManager
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var adsAdapter: AdsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        brandsAdapter.submitList(dummyBrands)
    }
}