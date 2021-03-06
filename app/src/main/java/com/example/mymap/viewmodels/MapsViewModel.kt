package com.example.mymap.viewmodels

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mymap.data.models.MyPlace
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

class MapsViewModel(var app: Application) : AndroidViewModel(app) {

    var startPlace = MutableLiveData<MyPlace>()
    var endPlace = MutableLiveData<MyPlace>()
    var isStartDirect = MutableLiveData<Boolean>(false)
    var lastKnownLocation = MutableLiveData<Location>()
    var selectionStartPlaceMethod = MutableLiveData<Int>(1)
    var selectionEndPlaceMethod = MutableLiveData<Int>(1)
    var travelMode = MutableLiveData<String>("")

    fun reversePlace(): String {
        if (startPlace.value != null && endPlace.value != null) {
            val tempPlace = startPlace.value
            startPlace.value = endPlace.value
            endPlace.value = tempPlace
            return "Succesfully"
        } else {
            return "Error"
        }
    }

    fun getAdressFromLocation(latLng: LatLng): Address? {
        try {
            val address = Geocoder(app).getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!address.isNullOrEmpty()) return address[0]
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getAddressFromName(name: String): Address? {
        try {
            val address = Geocoder(app).getFromLocationName(name, 1)
            if (!address.isNullOrEmpty()) return address[0]
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}