package com.example.helloworld
// package es.upm.btb.openweatherkt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.helloworld.ui.theme.HelloWorldTheme
// v1 added imports
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
// v2 added imports
import android.content.Intent
import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import android.util.Log
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf // for lat and long variables
import androidx.compose.runtime.getValue // ^^
import androidx.compose.runtime.setValue // ^^
import android.provider.Settings
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import android.widget.Toast

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    // state variables to hold the latitude and longitude
    private var latitude by mutableStateOf("0.0")
    private var longitude by mutableStateOf("0.0")
    private var latestLocation by mutableStateOf<Location?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: The activity is being created.")
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                AppSettingsButton() // for demonstration purposes (can revoke location permissions)
                SettingsButton()
                Column( // center the button
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Hello World!")

                    // button to second activity
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, SecondActivity::class.java)
                            startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF003eff), // background blue color
                            contentColor = Color.White // text color
                        )
                    ) {
                        Text("Go to Second Activity")
                    }

                    OpenStreetMapsButton(latestLocation = latestLocation) // composable button function for navigating to openstreetmapsactivity
                } // Column
            } // HelloWorldTheme
        }

        // v3
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionCode
            )
        } else {
            // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
            // whichever happens first
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        // update latitude and longitude state values
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
        latestLocation = location
        // log new location
        Log.v(TAG, "Location changed: Latitude: $latitude, Longitude: $longitude")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloWorldTheme {
        Greeting("World")
    }
}

@Composable
fun AppSettingsButton() { // for demonstration purposes (can revoke location permissions)
    val context = LocalContext.current // Get the context
    // Button to open App Info settings page
    Button(onClick = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null) // Get the package name from context
        intent.data = uri
        context.startActivity(intent) // Use context to start the activity
    }) {
        Text("Go to App Settings")
    }
}

@Composable
fun OpenStreetMapsButton(latestLocation: Location?) {
    val context = LocalContext.current // Get the context here
    // button for main -> openstreetmaps: Intent and location as parameter
    Button(onClick = {
        if (latestLocation != null) {
            val intent = Intent(context, OpenStreetMapsActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("location", latestLocation)
            intent.putExtra("locationBundle", bundle)
            context.startActivity(intent)
        } else {
            Log.e("MainActivity", "Location not set yet.")
            Toast.makeText(context, "Location not available.", Toast.LENGTH_SHORT).show() // Show toast if location is null
        }
    }) {
        Text(text = "Open OpenStreetMap")
    }
}

@Composable
fun SettingsButton() {
    val context = LocalContext.current // Get the current context

    Button(onClick = {
        try {
            // Open the Settings screen using an Intent
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Show a Toast message if opening Settings fails
            Toast.makeText(context, "Failed to open settings", Toast.LENGTH_SHORT).show()
        }
    }) {
        Text(text = "Open Settings")
    }
}