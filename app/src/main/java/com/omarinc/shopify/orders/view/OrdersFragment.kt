package com.omarinc.shopify.orders.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecastapplication.favouritesFeature.view.AdsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.BrandsAdapter
import com.example.weatherforecastapplication.favouritesFeature.view.OrdersAdapter
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentHomeBinding
import com.omarinc.shopify.databinding.FragmentOrdersBinding
import com.omarinc.shopify.home.view.HomeFragmentDirections
import com.omarinc.shopify.home.view.ZoomOutPageTransformer
import com.omarinc.shopify.home.viewmodel.HomeViewModel
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private lateinit var ordersManager: LinearLayoutManager
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var viewModel: OrdersViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(OrdersViewModel::class.java)

        viewModel
            .getCutomerOrders("a8f035227035120d9baa4a29f3d7b2c3")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrdersBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.apiState.collect{ result ->
                    when(result){
                        is ApiState.Loading ->{

                        }

                        is ApiState.Success ->{
                            ordersAdapter.submitList(result.response)
                        }
                        is ApiState.Failure ->{

                        }
                    }
                }
            }
        }


        val onBrandClick = { id : String ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductsFragment(
                    id
                )
            Navigation.findNavController(requireView()).navigate(action)
        }
        ordersAdapter = OrdersAdapter(
            requireContext(),
        )

        ordersManager = LinearLayoutManager(requireContext())
        ordersManager.orientation = LinearLayoutManager.VERTICAL
        binding.ordersRV.layoutManager = ordersManager
        binding.ordersRV.adapter = ordersAdapter

    }
}