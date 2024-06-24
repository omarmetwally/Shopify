package com.omarinc.shopify.productdetails.view.commentsbottomsheet

import com.omarinc.shopify.productdetails.view.CommentsAdapter
import com.omarinc.shopify.utilities.Helper
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentCommentsBottomSheetBinding
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModel
import com.omarinc.shopify.productdetails.viewModel.ProductDetailsViewModelFactory
import com.omarinc.shopify.favorites.model.FirebaseRepository
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.network.shopify.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.launch

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCommentsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var productId: String
    private val viewModel: ProductDetailsViewModel by viewModels {
        ProductDetailsViewModelFactory(
            ShopifyRepositoryImpl.getInstance(
                ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext()),
                CurrencyRemoteDataSourceImpl.getInstance(),
                AdminRemoteDataSourceImpl.getInstance()
            ),
            FirebaseRepository.getInstance()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId") ?: ""

        setupRecyclerView()
        loadComments()
    }

    private fun setupRecyclerView() {
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadComments() {
        val comments = Helper.generateStaticComments()
        val commentsAdapter = CommentsAdapter(comments)
        binding.commentsRecyclerView.adapter = commentsAdapter
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = resources.getDrawable(R.drawable.bottom_sheet_background, null)
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
