package com.omarinc.shopify.address.view

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
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

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cairo = LatLng(30.0, 30.0)
    private lateinit var googleMap: GoogleMap

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0


    private var currentMarker: Marker? = null

    private lateinit var city: String

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



        setListeners()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    private fun setListeners() {
        binding.chooseAddressButton.setOnClickListener {

            googleMap.let { map ->
                val location = getCurrentLocation(map)
                getCityFromLocation(location.latitude, location.longitude)
                if (city.isNotEmpty()) {
                    val action =
                    MapFragmentDirections.actionMapFragmentToAddressDetailsFragment(city)
                        findNavController().navigate(action)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a valid location",
                        Toast.LENGTH_SHORT
                    ).show()
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
            city = address.adminArea ?: "Unknown"


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


}
