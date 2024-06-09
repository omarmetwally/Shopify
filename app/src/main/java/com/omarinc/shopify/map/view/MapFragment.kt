package com.omarinc.shopify.map.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.omarinc.shopify.R
import com.omarinc.shopify.databinding.FragmentMapBinding
import com.omarinc.shopify.map.viewModel.MapViewModel
import com.omarinc.shopify.map.viewModel.MapViewModelFactory
import com.omarinc.shopify.model.ShopifyRepositoryImpl
import com.omarinc.shopify.models.CustomerAddress
import com.omarinc.shopify.network.ApiState
import com.omarinc.shopify.network.ShopifyRemoteDataSourceImpl
import com.omarinc.shopify.network.admin.AdminRemoteDataSourceImpl
import com.omarinc.shopify.network.currency.CurrencyRemoteDataSourceImpl
import com.omarinc.shopify.sharedPreferences.SharedPreferencesImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!


    private lateinit var viewModel: MapViewModel

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cairo = LatLng(30.0, 30.0)
    private lateinit var googleMap: GoogleMap

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    private var address: CustomerAddress = CustomerAddress("", "", "", "", "")

    private var currentMarker: Marker? = null

    companion object {
        private const val REQUEST_LOCATION_CODE = 505
        private const val TAG = "MapFragment"
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


    private fun setupViewModel() {
        val repository = ShopifyRepositoryImpl.getInstance(
            ShopifyRemoteDataSourceImpl.getInstance(requireContext()),
            SharedPreferencesImpl.getInstance(requireContext()),
            CurrencyRemoteDataSourceImpl.getInstance(),
            AdminRemoteDataSourceImpl.getInstance()
        )
        val factory = MapViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
    }

    private fun setListeners() {
        binding.chooseAddressButton.setOnClickListener {

            googleMap.let { map ->
                val location = getCurrentLocation(map)
                getCityFromLocation(location.latitude, location.longitude)

            }

            viewModel.createAddress(address)

            Log.i(TAG, "City: ${address.city}")
            lifecycleScope.launch {
                viewModel.address.collect { result ->

                    when (result) {
                        is ApiState.Failure -> Log.i(TAG, "Address failure ${result.msg} ")
                        ApiState.Loading -> Log.i(TAG, "Address loading")
                        is ApiState.Success -> {
                            Log.i(TAG, "ID:${result.response} ")
                            Toast.makeText(
                                requireContext(),
                                "Address added",
                                Toast.LENGTH_SHORT
                            ).show()
                            popFragment()
                        }

                    }

                }

            }

        }
        binding.currentLocationButton.setOnClickListener {

            setUpCurrentLocation()
            addMarkerWithCoordinates(latitude, longitude)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener(this)


        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cairo, 5f));

    }

    override fun onMapClick(latLng: LatLng) {

        currentMarker?.remove()

        getCityFromLocation(latLng.latitude, latLng.longitude)
        currentMarker = googleMap.addMarker(MarkerOptions().position(latLng).title("New Marker"))


    }


    private fun setUpCurrentLocation() {
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

                    getCityFromLocation(latitude, longitude)


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

    private fun getCityFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val cityName = address.adminArea ?: "Unknown"
            val countryName = address.countryName ?: "Unknown"
            val addressLine = address.getAddressLine(0) ?: "Unknown"

            this.address.apply {
                this.city = cityName
                this.country = countryName
                this.address1 = addressLine

            }
        } else {
            showToast("No address found for the provided location.")
        }
    }

    private fun getCurrentLocation(googleMap: GoogleMap): LatLng {
        val cameraPosition = googleMap.cameraPosition.target
        latitude = cameraPosition.latitude
        longitude = cameraPosition.longitude

        return LatLng(cameraPosition.latitude, cameraPosition.longitude)
    }

    private fun popFragment() {
        parentFragmentManager.popBackStack()
    }
}
