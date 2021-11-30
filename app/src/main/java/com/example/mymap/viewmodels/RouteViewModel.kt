package com.example.mymap.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.routing.*

class RouteViewModel(var app:Application) :AndroidViewModel(app) {
    var routePrimary = MutableLiveData<Route>()
    var routeSecond = MutableLiveData<Route>()
    var message = MutableLiveData<String>()
    private var routingEngine: RoutingEngine? = null
    init {
        try {
            routingEngine = RoutingEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of RoutingEngine failed: " + e.error.name);
        }
    }
    fun calculRoute(travelMode: String = "",waypoints: List<Waypoint>){
        when(travelMode){
            "car" -> {
                val carOptions = CarOptions()
                carOptions.routeOptions.alternatives = 2
                routingEngine?.calculateRoute(waypoints, carOptions, callback)
            }
            "scooter" -> {
                val scooterOptions = ScooterOptions()
                scooterOptions.routeOptions.alternatives = 2
                routingEngine?.calculateRoute(waypoints, scooterOptions, callback)
            }
            else -> {
                val pedestrianOptions = PedestrianOptions()
                pedestrianOptions.routeOptions.alternatives = 2
                routingEngine?.calculateRoute(waypoints, pedestrianOptions, callback)
            }
        }
    }
    private val callback = object : CalculateRouteCallback {
        override fun onRouteCalculated(
            routingError: RoutingError?,
            routes: MutableList<Route>?
        ) {
            if (routingError == null && !routes.isNullOrEmpty()) {
                val size = routes.size
                if (size >= 2) {
                    val route2 = routes.get(1)
                    routeSecond.value = route2
//                    drawLine(route2, secondaryColor)
//                    showDetail2(route2)
                }
                val route = routes.get(0)
                routePrimary.value = route
//                showDetail(route)
//                drawLine(route, primaryColor)
            } else {
                message.value = "Không tìm thấy tuyến đường"
//                Toast.makeText(this@MainActivity, "Không tìm thấy tuyến đường", Toast.LENGTH_LONG)
//                    .show()
            }
        }
    }
}