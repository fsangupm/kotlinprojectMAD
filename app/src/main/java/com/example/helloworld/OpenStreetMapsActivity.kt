package com.example.helloworld

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.location.Location
import android.util.Log
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import org.osmdroid.views.overlay.Polyline


class OpenStreetMapsActivity : AppCompatActivity() {
    private val TAG = "btaOpenStreetMapActivity"
    private lateinit var map: MapView

    // Use Google Maps to create a route of consistent coordinates: e.g.,"Points of interest at Campus Sur UPM"
    val gymkhanaCoords = listOf(
        GeoPoint(40.4211814, -3.6840355), // Cycle parking
        GeoPoint(40.4204166, -3.6842604), // Drinking Water
        GeoPoint(40.4189004, -3.6834093), // Memorial Statue
        GeoPoint(40.4170109, -3.6821419), // Drinking Water 2
        GeoPoint(40.4168662, -3.6801136), // Artwork Statue
        GeoPoint(40.4121633, -3.6791499), // Memorial Statue 2
        GeoPoint(40.4102632, -3.6801159), // Small Plaza
        GeoPoint(40.4110472, -3.6825404), // Small Plaza 2
        GeoPoint(40.4155487, -3.6840684), // Drinking Water 3
        GeoPoint(40.4183443, -3.6855094), // Drinking Water 4
        GeoPoint(40.4204282, -3.6858890), // Exit of Trail
    )
    val gymkhanaNames = listOf(
        "Cycle Parking",
        "Drinking Water",
        "Memorial Statue",
        "Drinkign Water 2",
        "Artwork Statue",
        "Memorial Statue 2",
        "Small Plaza",
        "Small Plaza 2",
        "Drinking Water 3",
        "Drinking Water 4",
        "Exit of Trail"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting activity...");
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_street_maps)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configure the user agent before loading the configuration
        // Configuration.getInstance().userAgentValue = BuildConfig.helloworld
        Configuration.getInstance().userAgentValue = "com.example.helloworld"
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE)) // testing
        // Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

        val bundle = intent.getBundleExtra("locationBundle")
        // val location: Location? = bundle?.getParcelable<Location>("location")
        val location: Location? = null // null for testing default coordinates
        val startPoint = if (location != null) {
            Log.d(TAG, "onCreate: Location[${location.altitude}][${location.latitude}][${location.longitude}]")
            GeoPoint(location.latitude, location.longitude)
        } else {
            Log.d(TAG, "onCreate: Location is null, using default coordinates")
            GeoPoint(40.389683644051864, -3.627825356970311)
        }
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(10.0) // experiment with zoom
        map.controller.setCenter(startPoint)

        // Add starting marker
        val marker = Marker(map)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_compass) as BitmapDrawable
        marker.title = "My current location"
        map.overlays.add(marker)

        // Add list of markers
        addGymkhanaMarkers(map, gymkhanaCoords, gymkhanaNames, this)
        addRouteMarkers(map, gymkhanaCoords, gymkhanaNames, this)
    }
}

fun addGymkhanaMarkers(map: MapView, coords: List<GeoPoint>, names: List<String>, context: Context) {
    for (i in coords.indices) {
        val marker = Marker(map)
        marker.position = coords[i]
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_camera) as BitmapDrawable
        marker.title = names[i]
        map.overlays.add(marker)
    }
}

// to define a route
fun addRouteMarkers(map: MapView, coords: List<GeoPoint>, names: List<String>, context: Context) {
    val polyline = Polyline()
    polyline.setPoints(coords)
    for (i in coords.indices) {
        val marker = Marker(map)
        marker.position = coords[i]
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_directions) as BitmapDrawable
        marker.title = names[i]
        map.overlays.add(marker)
    }
    map.overlays.add(polyline)
}