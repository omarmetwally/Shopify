package com.omarinc.shopify.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentLoginBinding
import com.omarinc.shopify.home.view.MainActivity
import com.omarinc.shopify.login.viewmodel.LoginViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val factory = LoginViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        binding.btnSkip.setOnClickListener {

            lifecycleScope.launch {
                viewModel.onSkipButtonPressed()
                viewModel.skipButtonState.collect { skipPressed ->
                    if (skipPressed) {
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }



        binding.txtRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)

        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailLoginEditTetx.text.toString().trim()
            val password = binding.passwordLoginEditText.text.toString().trim()

            viewModel.loginUser(email, password)
        }

        lifecycleScope.launch {
            viewModel.apiState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }

                    is ApiState.Failure -> {
                        Log.e("LoginFragment", "Login Failed", state.msg)
                        Toast.makeText(
                            context,
                            "Login Failed: ${state.msg.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ApiState.Loading -> {
                        Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}