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
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.android.material.snackbar.Snackbar
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentLoginBinding
import com.omarinc.shopify.home.view.MainActivity
import com.omarinc.shopify.login.viewmodel.LoginViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Helper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
        initViewModel()
        setupLoginButton()
        setupSkipButton()
        observeViewModel()
    }

    private fun initViewModel() {
        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val factory = LoginViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.emailLoginEditTetx.text.toString().trim()
            val password = binding.passwordLoginEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                binding.btnLogin.startAnimation()
                viewModel.loginUser(email, password)
            } else {
                showMissingFieldsError()
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLoginEditTetx.error = getString(R.string.email_is_required)
            isValid = false
        } else if (!Helper.validateEmail(email)) {
            binding.emailLoginEditTetx.error = getString(R.string.email_invalid)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordLoginEditText.error = getString(R.string.password_is_required)
            isValid = false
        }

        return isValid
    }

    private fun showMissingFieldsError() {
        if (binding.emailLoginEditTetx.text.toString().trim().isEmpty()) {
            binding.emailLoginEditTetx.error = getString(R.string.email_is_required)
        } else if (!Helper.validateEmail(binding.emailLoginEditTetx.text.toString().trim())) {
            binding.emailLoginEditTetx.error = getString(R.string.email_invalid)
        }
        if (binding.passwordLoginEditText.text.toString().trim().isEmpty()) {
            binding.passwordLoginEditText.error = getString(R.string.password_is_required)
        }
    }

    private fun setupSkipButton() {
        binding.btnSkip.setOnClickListener {
            showSkipAlertDialog()
        }
    }

    private fun showSkipAlertDialog() {
        Helper.showAlertDialog(
            context = requireContext(),
            title = getString(R.string.skip_alert_title),
            message = getString(R.string.skip_alert_message),
            positiveButtonText = getString(R.string.yes),
            positiveButtonAction = {
                skipAction()
            },
            negativeButtonText = getString(R.string.no)
        )
    }

    private fun skipAction() {
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.apiState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        binding.btnLogin.revertAnimation {
                            binding.btnLogin.text = getString(R.string.login_successful)
                        }

                        Snackbar.make(
                            binding.root,
                            "${getString(R.string.login_successful)}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        delay(500)
                        startActivity(Intent(requireActivity(), MainActivity::class.java))
                        requireActivity().finish()
                    }

                    is ApiState.Failure -> {
                        val errorMessage = getString(R.string.login_failed, state.msg.message)
                        binding.btnLogin.revertAnimation {
                            binding.btnLogin.text = errorMessage
                        }
                        Helper.showAlertDialog(
                            context = requireContext(),
                            title = getString(R.string.login_alert_Header),
                            message = errorMessage,
                            positiveButtonText = getString(R.string.ok)
                        )
                    }

                    ApiState.Loading -> {

                    }
                }
            }
        }
    }
}