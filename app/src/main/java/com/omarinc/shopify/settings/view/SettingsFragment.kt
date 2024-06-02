package com.omarinc.shopify.settings.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.shopify.databinding.FragmentSettingsBinding
import com.omarinc.shopify.models.Currencies
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.settings.viewModel.SettingsViewModel
import com.omarinc.shopify.settings.viewModel.SettingsViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

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
        settingsViewModel.getRequiredCurrency(Currencies.USD)
        lifecycleScope.launch {
            settingsViewModel.requiredCurrency.collect { result ->
                when (result) {
                    is ApiState.Failure -> Log.i(
                        TAG,
                        "onViewCreated: Failure ${result.msg.message}"
                    )

                    ApiState.Loading -> Log.i(TAG, "onViewCreated: Loading")
                    is ApiState.Success -> {

                        val currency = result.response.data["USD"]
                        Log.i(
                            TAG,
                            "onViewCreated: Success code=${currency?.code} value=${currency?.value} last update ${result.response.meta.last_updated_at}"
                        )
                        currency?.value?.let { value ->
                            currency.code.let { unit ->
                                settingsViewModel.setCurrency(
                                    value,
                                    unit
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    private fun setUpViewModel() {

       /* val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        settingsViewModelFactory = SettingsViewModelFactory(repository)*/
        settingsViewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]
    }
}


