package com.omarinc.shopify.registration.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.shopify.databinding.FragmentRegistrationBinding
import com.omarinc.shopify.registration.viewmodel.RegistrationViewModel
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.registration.viewModel.RegistrationViewModelFactory
import kotlinx.coroutines.flow.collect
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

        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext())
        )
        val factory = RegistrationViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory).get(RegistrationViewModel::class.java)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.nameRegisterEditText.text.toString().trim()
            val email = binding.emailRegisterEditText.text.toString().trim()
            val password = binding.passwordRegisterEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordRegisterEditText.text.toString().trim()

            if (password == confirmPassword) {
                viewModel.registerUser(email, password, fullName)
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            viewModel.apiState.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                    }
                    is ApiState.Failure -> {
                        Log.e("RegistrationFragment", "Registration Failed", state.msg)
                        Toast.makeText(context, "Registration Failed: ${state.msg.message}", Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
