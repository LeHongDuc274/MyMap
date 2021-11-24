package com.example.mymap.viewmodels

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mymap.data.models.MyPlace
import com.google.android.gms.location.zzt
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place

class MapsViewModel(var app: Application) : AndroidViewModel(app) {

    var newAddress = MutableLiveData<Address>()
    var startPlace = MutableLiveData<MyPlace>()
    var endPlace = MutableLiveData<MyPlace>()
    var isStartDirect = MutableLiveData<Boolean>(false)
    var lastKnownLocation = MutableLiveData<Location>()
    fun addNewAddress(latLng: LatLng) {
        val address = Geocoder(app).getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (address[0].maxAddressLineIndex >= 0){
           newAddress.value = address[0]
        }
    }

}