package com.example.mymap.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.mymap.Contains.Companion.conVertMetToString
import com.example.mymap.Contains.Companion.convertTimeToString
import com.example.mymap.R
import com.example.mymap.data.models.MyPlace
import com.example.mymap.viewmodels.RouteViewModel
import com.here.sdk.core.*
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.mapview.*
import com.here.sdk.routing.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private var startPoint: MyPlace? = null
    private var endPoint: MyPlace? = null
    private var startGeoCoordinates: GeoCoordinates? = null
    private var endGeoCoordinates: GeoCoordinates? = null
    private var travelMode: String = ""
    private var primaryColor = Color.valueOf(0f, 0f, 1f, 0.8f)
    private var secondaryColor = Color.valueOf(0f, 0.5f, 0f, 0.2f)
    lateinit var routeViewModel: RouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        routeViewModel = ViewModelProvider(this)[RouteViewModel::class.java]
        getArgs()
        mapView = findViewById(R.id.map_view_here)
        mapView.onCreate(savedInstanceState)
        obveser()
        loadMap()
        initControl()
    }

    private fun getArgs() {
        startPoint = intent.getSerializableExtra("start") as MyPlace
        endPoint = intent.getSerializableExtra("end") as MyPlace
        travelMode = intent.getStringExtra("mode").toString()
        if (startPoint != null) startGeoCoordinates = GeoCoordinates(
            startPoint!!.latitude,
            startPoint!!.longtitude
        )
        if (endPoint != null) endGeoCoordinates =
            GeoCoordinates(endPoint!!.latitude, endPoint!!.longtitude)
    }

    private fun obveser() {
        routeViewModel.message.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        routeViewModel.routePrimary.observe(this) {
            it?.let { mRoute ->
                drawLine(mRoute, primaryColor)
                showDetail(mRoute)
            }
        }
        routeViewModel.routeSecond.observe(this) {
            it?.let { mRoute ->
                drawLine(mRoute, secondaryColor)
                showDetail2(mRoute)
            }
        }
    }

    private fun initControl() {
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun loadMap() {
        mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
            if (mapError == null) {
                addMarker()
                addRouter()
            } else {
                Toast.makeText(this, "Không thể tải bản đồ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarker() {
        val mapImage = MapImageFactory.fromResource(resources, R.drawable.location_pin_40)
        if (startGeoCoordinates != null && endGeoCoordinates != null) {
            val listGeoCoordinates = mutableListOf(
                MapMarker(startGeoCoordinates!!, mapImage),
                MapMarker(endGeoCoordinates!!, mapImage)
            )
            mapView.mapScene.addMapMarkers(listGeoCoordinates)
            mapView.camera.lookAt(startGeoCoordinates!!, 10000.0)
        }
    }

    private fun addRouter() {
        val startWaypoint = Waypoint(startGeoCoordinates!!)
        val endWaypoint = Waypoint(endGeoCoordinates!!)
        val waypoints: List<Waypoint> = ArrayList(Arrays.asList(startWaypoint, endWaypoint))
        routeViewModel.calculRoute(travelMode, waypoints)
    }

    private fun drawLine(route: Route, color: Color) {
        var routerGeoPolyLine: GeoPolyline? = null
        val widthInPixels: Double = 15.0
        try {
            routerGeoPolyLine = GeoPolyline(route.polyline)
        } catch (e: InstantiationErrorException) {
            return
        }
        val routeMapPolyLine = MapPolyline(routerGeoPolyLine, widthInPixels, color)
        mapView.mapScene.addMapPolyline(routeMapPolyLine)
    }

    private fun showDetail(route: Route) {
        // primary
        val timeCostPrimary = route.durationInSeconds
        val distancePrimary = route.lengthInMeters
        findViewById<TextView>(R.id.tv_distance).text = conVertMetToString(distancePrimary)
        findViewById<TextView>(R.id.tv_time).text = convertTimeToString(timeCostPrimary)
        findViewById<ProgressBar>(R.id.progress_circular).visibility = View.GONE
    }

    private fun showDetail2(route2: Route) {
        // second route
        val timeCostSecondary = route2.durationInSeconds
        val distanceSecondary = route2.lengthInMeters
        findViewById<TextView>(R.id.tv_distance_second).text = conVertMetToString(distanceSecondary)
        findViewById<TextView>(R.id.tv_time_second).text = convertTimeToString(timeCostSecondary)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}