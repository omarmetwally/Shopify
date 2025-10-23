package com.omarinc.shopify.registration.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentRegistrationBinding
import com.omarinc.shopify.registration.viewmodel.RegistrationViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.registration.viewModel.RegistrationViewModelFactory
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import com.omarinc.shopify.utilities.Helper
import com.omarinc.shopify.utilities.Helper.validateEmail
import com.omarinc.shopify.utilities.Helper.validatePassword
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment() {

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRegisterButton()
        observeViewModel()
        binding.imgBackInRegister.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupViewModel() {
        val sharedPreferences = SharedPreferencesImpl.getInstance(requireContext())
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            sharedPreferences,
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val factory = RegistrationViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory).get(RegistrationViewModel::class.java)
    }

    private fun setupRegisterButton() {

        binding.btnRegister.setOnClickListener {
            val fullName = binding.nameRegisterEditText.text.toString().trim()
            val email = binding.emailRegisterEditText.text.toString().trim()
            val password = binding.passwordRegisterEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordRegisterEditText.text.toString().trim()
            val  phoneNumber = binding.PhoneEditText.text.toString().trim()

            if (validateInputs(fullName, email, password, confirmPassword,phoneNumber)) {
                if (password == confirmPassword) {
                    if (validatePassword(password)) {
                        binding.btnRegister.startAnimation()
                        viewModel.registerUser(email, password, fullName, phoneNumber)
                    } else {
                        binding.passwordRegisterEditText.error =
                            getString(R.string.password_validation)
                    }
                } else {
                    binding.confirmPasswordRegisterEditText.error =
                        getString(R.string.password_not_matching)
                }
            } else {
                showMissingFieldsError()
            }
        }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String
    ): Boolean {
        var isValid = true

        if (fullName.isEmpty()) {
            binding.nameRegisterEditText.error = getString(R.string.name_is_required)
            isValid = false
        }

        if (phoneNumber.isEmpty()) {
            binding.PhoneEditText.error = getString(R.string.phone_number_is_required)
            isValid = false
        } else if (!Helper.validatePhoneNumber(phoneNumber)) {
            binding.PhoneEditText.error = getString(R.string.phone_invalid)
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailRegisterEditText.error = getString(R.string.email_is_required)
            isValid = false
        } else if (!validateEmail(email)) {
            binding.emailRegisterEditText.error = getString(R.string.email_invalid)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordRegisterEditText.error = getString(R.string.password_is_required)
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordRegisterEditText.error =
                getString(R.string.confirm_password_is_required)
            isValid = false
        }

        return isValid
    }

    private fun showMissingFieldsError() {
        if (binding.nameRegisterEditText.text.toString().trim().isEmpty()) {
            binding.nameRegisterEditText.error = getString(R.string.name_is_required)
        }
        if (binding.emailRegisterEditText.text.toString().trim().isEmpty()) {
            binding.emailRegisterEditText.error = getString(R.string.email_is_required)
        }
        if (binding.passwordRegisterEditText.text.toString().trim().isEmpty()) {
            binding.passwordRegisterEditText.error = getString(R.string.password_is_required)
        }
        if (binding.confirmPasswordRegisterEditText.text.toString().trim().isEmpty()) {
            binding.confirmPasswordRegisterEditText.error =
                getString(R.string.confirm_password_is_required)
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.apiState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        binding.btnRegister.revertAnimation()
                        Helper.showAlertDialog(
                            context = requireContext(),
                            title = getString(R.string.registration_alert_Header),
                            message = getString(R.string.registration_successful),
                            positiveButtonText = getString(R.string.ok)
                        )
                    }

                    is ApiState.Failure -> {
                        val errorMessage =
                            getString(R.string.registration_failed, state.msg.message)
                        binding.btnRegister.revertAnimation()
                        Helper.showAlertDialog(
                            context = requireContext(),
                            title = getString(R.string.registration_alert_Header),
                            message = errorMessage,
                            positiveButtonText = getString(R.string.ok)
                        )
                    }

                    ApiState.Loading -> {
                        Toast.makeText(context, getString(R.string.loading), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}