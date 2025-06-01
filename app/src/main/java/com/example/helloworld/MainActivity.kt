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
import android.widget.EditText
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

// v5 added imports
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box


class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    // state variables to hold the latitude and longitude
    private var latitude by mutableStateOf("0.0")
    private var longitude by mutableStateOf("0.0")
    private var latestLocation by mutableStateOf<Location?>(null)
    private var isLocationEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: The activity is being created.")
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                MainScreen()
            } // HelloWorldTheme
        }

        // v3
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        val userIdentifier = getUserIdentifier(context)
        var showDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My App") },
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(context, SecondActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.List, contentDescription = "Second Activity")
                        }
                        IconButton(onClick = {
                            val intent = Intent(context, OpenStreetMapsActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Place, contentDescription = "Map")
                        }
                        IconButton(onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }, // topBAr
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    // empty for now
                }
            }
        )

        Column( // center the button
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()), // Add padding for system bars
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Hello World!")

            // commented out buttons
            /*
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
            */

            Button( // User ID button
                onClick = {
                    if (userIdentifier != null) { // if User ID is already saved
                        Toast.makeText(context, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
                    }
                    showDialog = true
                }
            ) {
                Text("Enter User ID")
            }

            LocationSwitch( // Location Switch
                isChecked = isLocationEnabled,
                onCheckedChange = { isChecked ->
                    isLocationEnabled = isChecked
                    if (isChecked) {
                        startLocationUpdates()
                    } else {
                        stopLocationUpdates()
                    }
                }
            )

            // Show the dialog if the user identifier is not saved
            if (userIdentifier == null) {
                showDialog = true
            }
            if (showDialog) {
                ShowUserIDDialogue(
                    onDismiss = { showDialog = false },
                    onSave = { newUserIdentifier ->
                        saveUserIdentifier(newUserIdentifier)
                        Toast.makeText(context, "User ID saved: $newUserIdentifier", Toast.LENGTH_LONG).show()
                        showDialog = false // Close the dialog after saving
                    }
                )
            }
        } // Column
    } // MainScreen

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
        // Log.v(TAG, "Location changed: Latitude: $latitude, Longitude: $longitude")
        // show toast with new location
        val toastText = "New location: ${location.latitude}, ${location.longitude}"
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
        saveCoordinatesToFile(location.latitude, location.longitude)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // v4 persistence
    private fun saveUserIdentifier(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }
    private fun getUserIdentifier(context: Context): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    private fun startLocationUpdates() {
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }
    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    @Composable
    fun ShowUserIDDialogue(onDismiss: () -> Unit, onSave: (String) -> Unit) {
        var context = LocalContext.current
        var userInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Enter User Identifier") },
            text = {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("User Identifier") },
                    modifier = Modifier.padding(16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            onSave(userInput)
                        } else {
                            Toast.makeText(context, "User ID cannot be blank", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double) {
        val fileName = "gps_coordinates.csv"
        val file = File(filesDir, fileName)
        val timestamp = System.currentTimeMillis()
        file.appendText("$timestamp;$latitude;$longitude\n")
    }
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

@Composable
fun LocationSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, // align text and switch vertically
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = if (isChecked) "Disable location" else "Enable location",
            fontSize = 12.sp // smaller font
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
