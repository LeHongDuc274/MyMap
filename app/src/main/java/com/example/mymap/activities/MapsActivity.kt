package com.example.mymap.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mymap.R
import com.example.mymap.data.models.MyPlace

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.mymap.databinding.ActivityMapsBinding
import com.example.mymap.fragment.DirectionsFragment
import com.example.mymap.fragment.DirectionsFragment.Companion.REQUEST_END_LOCAL
import com.example.mymap.fragment.DirectionsFragment.Companion.REQUEST_START_LOCAL
import com.example.mymap.viewmodels.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private lateinit var viewmodel: MapsViewModel
    private var marker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        viewmodel = ViewModelProvider(this)[MapsViewModel::class.java]

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var fragment = DirectionsFragment()
        binding.fabGo.setOnClickListener {
            // showCurrentPlace()
            fragment.show(supportFragmentManager, "bottomSheet")
        }
        binding.btnSearch.setOnClickListener {
            val query = binding.edtSearch.text.toString()
            if (!query.isEmpty()) {
                val address = Geocoder(this).getFromLocationName(query, 1)
                if (address.isNotEmpty()) {
                    val result = address[0]
                    marker?.remove()
                    marker = map?.addMarker(
                        MarkerOptions().position(LatLng(result.latitude, result.longitude))
                            .title("Lat:${result.latitude} , Lon: ${result.longitude}")
                            .snippet(result.getAddressLine(0))
                    )
                    marker?.showInfoWindow()
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                result.latitude,
                                result.longitude
                            ), DEFAULT_ZOOM.toFloat()
                        )
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        map.setOnMapLongClickListener {
            addNewMarker(it)
        }
    }

    private fun addNewMarker(latLng: LatLng) {
        var address = mutableListOf<Address>()
        address = Geocoder(this).getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (address.isNotEmpty()) {
            marker?.remove()
            val add = address[0].getAddressLine(0)
            marker = map?.addMarker(
                MarkerOptions().position(latLng)
                    .title("Lat:${latLng.latitude} , Lon: ${latLng.longitude}").snippet("$add")
            )
            marker?.showInfoWindow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        map?.let { map ->
            try {
                if (locationPermissionGranted) {
                    map.isMyLocationEnabled = true
                    map.uiSettings?.isMyLocationButtonEnabled = true
                } else {
                    map.isMyLocationEnabled = false
                    map.uiSettings?.isMyLocationButtonEnabled = false
                    lastKnownLocation = null
                    getLocationPermission()
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message, e)
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }

}