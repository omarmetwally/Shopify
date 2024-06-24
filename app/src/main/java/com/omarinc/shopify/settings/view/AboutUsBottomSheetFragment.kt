package com.omarinc.shopify.settings.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentAboutUsBottomSheetBinding

class AboutUsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAboutUsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutUsBottomSheetBinding.inflate(inflater, container, false)
        Log.d("AboutUsBottomSheet", "onCreateView called")
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("AboutUsBottomSheet", "onDestroyView called")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
             bottomSheet?.background = resources.getDrawable(R.drawable.bottom_sheet_background, null)
            Log.d("AboutUsBottomSheet", "onCreateDialog setOnShowListener called")
        }
        return dialog
    }
}
