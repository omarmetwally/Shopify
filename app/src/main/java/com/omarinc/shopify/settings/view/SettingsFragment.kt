package com.omarinc.shopify.settings.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.shopify.databinding.FragmentSettingsBinding
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.settings.viewModel.SettingsViewModel
import com.omarinc.shopify.settings.viewModel.SettingsViewModelFactory
import com.omarinc.shopify.sharedpreferences.SharedPreferencesImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val TAG = "SettingsFragment"
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewModel()
        setUpSpinner()

    }

    private fun setUpViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        settingsViewModelFactory = SettingsViewModelFactory(repository)
        settingsViewModel =
            ViewModelProvider(this, settingsViewModelFactory)[SettingsViewModel::class.java]
    }

    private fun setUpSpinner() {
        val currencies = Currencies.values()
        val currencyNames = currencies.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerCurrencies.adapter = adapter
        binding.spinnerCurrencies.onItemSelectedListener = this
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedCurrency = Currencies.values()[position].name
        settingsViewModel.setCurrency(selectedCurrency)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
