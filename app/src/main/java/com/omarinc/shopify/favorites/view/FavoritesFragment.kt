package com.omarinc.shopify.favorites.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omarinc.shopify.databinding.FragmentFavoritesBinding
import com.omarinc.shopify.favorites.model.FavoriteItemFirebase
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModel
import com.omarinc.shopify.favorites.viewmodel.FavoriteViewModelFactory
import com.omarinc.shopify.models.CartProduct
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Constants
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val favoriteFactory = FavoriteViewModelFactory(FirebaseRepository.getInstance())
        viewModel = ViewModelProvider(this, favoriteFactory).get(FavoriteViewModel::class.java)

        favoritesAdapter = FavoritesAdapter(requireContext()) { productId ->
            val action = FavoritesFragmentDirections.actionFavoritesFragmentToProductDetailsFragment(productId)
            findNavController().navigate(action)
        }
        binding.rvFavorites.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvFavorites.adapter = favoritesAdapter

        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val userToken = sharedPreferences.readStringFromSharedPreferences(Constants.USER_TOKEN)

        viewModel.getFavorites(userToken)

        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }
        lifecycleScope.launch {
            viewModel.favorites.collect { favoriteItems ->
                setupNoFavourites(favoriteItems)
                favoritesAdapter.submitList(favoriteItems)
            }
        }

    }

    private fun setupNoFavourites(items:List<FavoriteItemFirebase>) {
        if (items.isEmpty()){
            binding.noFavorites .visibility = View.VISIBLE
        }
    }

}
