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
import com.example.helloworld.network.RetrofitClient
import com.example.helloworld.network.OverpassResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // val location: Location? = bundle?.getParcelable<Location>("location")
        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location")
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

        // integration of Overpass API to fetch bike route locations
        val lat = startPoint.latitude
        val lon = startPoint.longitude
        val overpassQuery = """
            [out:json];
            (
              node(around:1000,$lat,$lon)["amenity"="drinking_water"];
              node(around:1000,$lat,$lon)["amenity"="cycle_parking"];
              node(around:1000,$lat,$lon)["tourism"="attraction"];
              way(around:3000,$lat,$lon)["highway"~"cycleway|footway"];
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        RetrofitClient.overpassService.queryOverpass(overpassQuery)
            .enqueue(object : Callback<OverpassResponse> {
                override fun onResponse(call: Call<OverpassResponse>, response: Response<OverpassResponse>) {
                    if (response.isSuccessful) {
                        val elements = response.body()?.elements ?: emptyList()
                        // Step 1: Map node IDs to GeoPoints
                        val nodeMap = mutableMapOf<Long, GeoPoint>()
                        val ways = mutableListOf<List<GeoPoint>>()
                        val poiMarkers = mutableListOf<GeoPoint>()

                        for (element in elements) { // map OSM nodes
                            if (element.type == "node" && element.lat != null && element.lon != null) {
                                nodeMap[element.id] = GeoPoint(element.lat, element.lon)
                            }
                        }

                        // Step 2: Extract and convert ways to polyline segments
                        for (element in elements) {
                            if (element.type == "way" && element.nodes != null) {
                                val geoPoints = element.nodes.mapNotNull { nodeMap[it] }
                                if (geoPoints.size >= 2) {
                                    ways.add(geoPoints)
                                }
                            }
                        }

                        // Step 3: Add markers for POIs
                        for (element in elements) {
                            if (element.type == "node" && element.tags != null) {
                                val geoPoint = GeoPoint(element.lat!!, element.lon!!)
                                poiMarkers.add(geoPoint)
                                val marker = Marker(map)
                                marker.position = geoPoint
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                val icon = when {
                                    element.tags["amenity"] == "cycle_parking" -> android.R.drawable.ic_menu_directions
                                    element.tags["amenity"] == "drinking_water" -> android.R.drawable.ic_menu_gallery
                                    element.tags["tourism"] == "attraction" -> android.R.drawable.ic_menu_compass
                                    else -> android.R.drawable.ic_menu_info_details
                                }
                                marker.icon = ContextCompat.getDrawable(this@OpenStreetMapsActivity, icon) as BitmapDrawable
                                marker.title = element.tags["name"] ?: "POI"
                                map.overlays.add(marker)
                            }
                        }

                        if (ways.isEmpty()) return

                        // Calculate centroid
                        val allPoints = ways.flatten()
                        val centroidLat = allPoints.map { it.latitude }.average()
                        val centroidLon = allPoints.map { it.longitude }.average()
                        val centroid = GeoPoint(centroidLat, centroidLon)

                        // Sort segments by angle from centroid
                        fun angleToCentroid(point: GeoPoint): Double {
                            return Math.atan2(point.latitude - centroid.latitude, point.longitude - centroid.longitude)
                        }

                        val sorted = ways
                            .mapNotNull { seg -> seg.firstOrNull()?.let { Pair(seg, angleToCentroid(it)) } }
                            .sortedBy { it.second }
                            .map { it.first }

                        // Select ~10 segments (between 8-14)
                        val selectedSegments = sorted.chunked(sorted.size / 10).mapNotNull { it.firstOrNull() }.take(14)

                        // Chain them into one list and loop back
                        val circularRoute = selectedSegments.flatMap { it }.distinct().toMutableList()
                        if (circularRoute.size > 2) circularRoute.add(circularRoute.first()) // Close loop

                        // Draw the route
                        val polyline = Polyline().apply {
                            setPoints(circularRoute)
                            width = 5f
                        }
                        map.overlays.add(polyline)
                        map.invalidate()

                    }
                }

                override fun onFailure(call: Call<OverpassResponse>, t: Throwable) {
                    Log.e(TAG, "Failed to fetch OSM data: ${t.message}")
                }
            })


        // Add starting marker
        val marker = Marker(map)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_compass) as BitmapDrawable
        marker.title = "My current location"
        map.overlays.add(marker)

        // Add list of markers
//        addGymkhanaMarkers(map, gymkhanaCoords, gymkhanaNames, this)
//        addRouteMarkers(map, gymkhanaCoords, gymkhanaNames, this)
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

fun drawPOIConnectors(map: MapView, pois: List<GeoPoint>, cycleways: List<List<GeoPoint>>) {
    for (poi in pois) {
        var minDist = Double.MAX_VALUE
        var closestPoint: GeoPoint? = null
        for (poly in cycleways) {
            for (point in poly) {
                val dist = poi.distanceToAsDouble(point)
                if (dist < minDist) {
                    minDist = dist
                    closestPoint = point
                }
            }
        }
        if (closestPoint != null && minDist < 100.0) {
            val line = Polyline()
            line.setPoints(listOf(poi, closestPoint))
            line.width = 2f
            line.color = android.graphics.Color.GRAY
            map.overlays.add(line)
        }
    }
}

fun generateScenicRoute(segments: List<List<GeoPoint>>, maxDistanceKm: Double): List<GeoPoint> {
    if (segments.isEmpty()) return emptyList()

    val used = mutableSetOf<Int>()
    val route = mutableListOf<GeoPoint>()
    var totalDistance = 0.0

    val maxDistanceMeters = maxDistanceKm * 1000

    var current = segments.maxByOrNull { segment ->
        segment.zipWithNext { a, b -> a.distanceToAsDouble(b) }.sum()
    } ?: return emptyList()

    route.addAll(current)
    used.add(segments.indexOf(current))

    fun angleBetween(p1: GeoPoint, p2: GeoPoint, p3: GeoPoint): Double {
        val v1 = doubleArrayOf(p2.latitude - p1.latitude, p2.longitude - p1.longitude)
        val v2 = doubleArrayOf(p3.latitude - p2.latitude, p3.longitude - p2.longitude)
        val dot = v1[0] * v2[0] + v1[1] * v2[1]
        val mag1 = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1])
        val mag2 = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1])
        val cosAngle = dot / (mag1 * mag2 + 1e-10)
        return Math.toDegrees(Math.acos(cosAngle.coerceIn(-1.0, 1.0)))
    }

    while (totalDistance < maxDistanceMeters) {
        val lastPoint = route.lastOrNull() ?: break
        val prevPoint = if (route.size >= 2) route[route.size - 2] else lastPoint

        val next = segments.withIndex()
            .filter { (i, s) -> i !in used && s.isNotEmpty() }
            .map { (i, s) ->
                val dist = lastPoint.distanceToAsDouble(s.first())
                val angle = angleBetween(prevPoint, lastPoint, s.first())
                Triple(i, s, Pair(dist, angle))
            }
            .filter { (_, _, pair) -> pair.first < 250 && pair.second < 90 } // filter sharp or far segments
            .minByOrNull { (_, _, pair) -> pair.first + pair.second }

        if (next != null) {
            val (index, segment, _) = next
            val segmentDistance = segment.zipWithNext { a, b -> a.distanceToAsDouble(b) }.sum()
            if (totalDistance + segmentDistance > maxDistanceMeters) break
            route.addAll(segment)
            used.add(index)
            totalDistance += segmentDistance
        } else break
    }

    // Remove redundant points closer than 20m
    val simplified = mutableListOf<GeoPoint>()
    val tolerance = 20.0
    for (point in route) {
        if (simplified.isEmpty() || point.distanceToAsDouble(simplified.last()) > tolerance) {
            simplified.add(point)
        }
    }

    return simplified
}

