package com.example.mymap.data.models

import java.io.Serializable

data class MyPlace(var latitude: Double,var longtitude : Double, var title : String, var snippet : String) : Serializable
