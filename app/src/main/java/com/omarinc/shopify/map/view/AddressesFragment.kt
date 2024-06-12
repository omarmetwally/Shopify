package com.omarinc.shopify.map.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentAddressesBinding

class AddressesFragment : Fragment() {

    companion object{
        private const val TAG = "AddressesFragment"
    }

    private var _binding: FragmentAddressesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}