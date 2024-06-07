package com.omarinc.shopify.map.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentMapBinding
import com.omarinc.shopify.map.viewModel.MapViewModel
import com.omarinc.shopify.map.viewModel.MapViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!


    private lateinit var viewModel: MapViewModel

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cairo = LatLng(30.0, 30.0)
    private lateinit var googleMap: GoogleMap

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    companion object {
        private const val REQUEST_LOCATION_CODE = 505

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setListeners()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setListeners() {
        binding.chooseAddressButton.setOnClickListener {

        }
        binding.currentLocationButton.setOnClickListener {

            setUpLocation()
            addMarkerWithCoordinates(latitude, longitude)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener(this)



        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cairo, 10f))
    }

    override fun onMapClick(latLng: LatLng) {
        // Handle map click events
        googleMap.addMarker(MarkerOptions().position(latLng).title("New Marker"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance()
        )
        val factory = MapViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
    }

    private fun setUpLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                requestLocationUpdates()
            } else {
                enableLocationServices()
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestLocationUpdates() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)


                    val location = p0.lastLocation
                    latitude = location?.latitude ?: 0.0
                    longitude = location?.longitude ?: 0.0




                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            },
            Looper.getMainLooper()
        )
    }


    private fun enableLocationServices() {
        showToast("Turn on your location")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION_CODE
        )
    }

    private fun addMarkerWithCoordinates(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions().position(location).title("Marker at (${latitude}, ${longitude})")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
