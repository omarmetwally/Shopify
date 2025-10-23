package com.omarinc.shopify.address.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.omarinc.shopify.databinding.FragmentAddressDetailsBinding
import com.omarinc.shopify.address.viewModel.AddressViewModel
import com.omarinc.shopify.address.viewModel.AddressViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch


class AddressDetailsFragment : Fragment() {

    companion object {
        private const val TAG = "AddressDetailsFragment"
    }

    private var _binding: FragmentAddressDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddressViewModel

    private val args: AddressDetailsFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddressDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupViewModel()
        setListeners()

    }

    private fun setListeners() {
        binding.submitAddressButton.setOnClickListener {
            val addressDetails = extractAddressDetails()
            setAddress(addressDetails)
        }
    }

    private fun extractAddressDetails(): CustomerAddress {

        val firstName = binding.FirstNameEditText.text.toString()
        val lastName = binding.lastNameEditText.text.toString()
        val address1 = binding.streetEditText.text.toString()
        val phone = binding.phoneEditText.text.toString()
        val city = args.city


        return CustomerAddress("", address1, "", city, "Egypt", firstName, lastName, phone)

    }


    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val factory = AddressViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AddressViewModel::class.java]
    }

    private fun setAddress(address: CustomerAddress) {
        viewModel.createAddress(address)
        Log.i(TAG, "City: ${address.city}")
        lifecycleScope.launch {
            viewModel.address.collect { result ->

                when (result) {
                    is ApiState.Failure -> Log.i(TAG, "Address failure ${result.msg} ")
                    ApiState.Loading -> Log.i(TAG, "Address loading")
                    is ApiState.Success -> {
                        Log.i(TAG, "ID:${result.response} ")
                        Toast.makeText(
                            requireContext(),
                            "Address added",
                            Toast.LENGTH_SHORT
                        ).show()
                        popFragment()
                    }
                }
            }
        }
    }

    private fun popFragment() {
        findNavController().navigateUp()
        findNavController().navigateUp()
    }
}