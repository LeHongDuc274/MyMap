package com.example.mymap.activities

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mymap.Contains.Companion.RESULT
import com.example.mymap.Contains.Companion.SEND_INT_KEY_CODE
import com.example.mymap.R
import com.example.mymap.data.models.MyPlace

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymap.databinding.ActivityChooseLocalBinding
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import java.io.IOException

class ChooseLocalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityChooseLocalBinding
    private var marker: Marker? = null
    private var place: MyPlace? = null
    private var KEY_CODE : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChooseLocalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        KEY_CODE = getIntent().getIntExtra(SEND_INT_KEY_CODE,0)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener {
            try {
                val address = Geocoder(this).getFromLocation(it.latitude, it.longitude, 1)
                if (address.isNotEmpty()) {
                    marker?.remove()
                    val add = address[0].getAddressLine(0)
                    marker = mMap.addMarker(
                        MarkerOptions().position(it)
                            .title("Lat:${it.latitude} , Lon: ${it.longitude}").snippet("$add")
                    )
                    marker?.showInfoWindow()
                }
                marker?.let {
                    place = MyPlace(
                        it.position.latitude,
                        it.position.longitude,
                        it.title,
                        it.snippet
                    )
                }
                binding.tvLocal.text = marker?.snippet
            } catch (e : IOException){
                e.printStackTrace()
            }
        }
        binding.btnOk.setOnClickListener {
            if (place != null) {
                val intent = Intent()
                intent.putExtra(RESULT, place)
                setResult(KEY_CODE,intent)
                finish()
            } else {
                Toast.makeText(this,"Chua chon dia diem",Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
}