package com.omarinc.shopify.productdetails.view.zoomimagefragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.omarinc.shopify.databinding.FragmentZoomImageBinding
import com.davemorrissey.labs.subscaleview.ImageSource

class ZoomImageFragment : Fragment() {

    private lateinit var binding: FragmentZoomImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZoomImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackInZoom.setOnClickListener {
            findNavController().navigateUp()
        }
        val imageUrl = arguments?.getString("imageUrl")
        imageUrl?.let {
            loadImage(it)
        }
        binding.imageView.maxScale = 10f
        binding.imageView.minScale = 1f
    }

    private fun loadImage(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(
                    resource: android.graphics.Bitmap,
                    transition: Transition<in android.graphics.Bitmap>?
                ) {
                    binding.imageView.setImage(ImageSource.bitmap(resource))
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // Handle placeholder if needed
                }
            })
    }
}
