package com.example.helloworld

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// v2 added imports
import com.example.helloworld.ui.theme.HelloWorldTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*  // For layouts like Column, Row, Spacer, etc.
import androidx.compose.material3.Button  // For Button composable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text  // For Text composable
import androidx.compose.ui.Alignment  // For alignment options
import androidx.compose.ui.Modifier  // For applying modifiers to composables
import android.content.Intent
import android.util.Log
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.mutableStateOf
import java.io.IOException
import androidx.compose.runtime.remember

//v5 added imports
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable

class SecondActivity : AppCompatActivity() {
    private val TAG = "btaSecondActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.")

        val fileContentsState = mutableStateOf("Loading...")
        fileContentsState.value = readFileContents()

        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            HelloWorldTheme {
                val fileContents = fileContentsState.value
                val rows = fileContents.trim().lines().mapNotNull { line ->
                    val parts = line.split(";")
                    if (parts.size >= 3) {
                        val timestamp = parts[0].toLongOrNull()
                        val lat = parts[1].toDoubleOrNull()
                        val lon = parts[2].toDoubleOrNull()
                        if (timestamp != null && lat != null && lon != null) {
                            Triple(timestamp, lat, lon)
                        } else null
                    } else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text("GPS Coordinate Log", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date/Time", Modifier.weight(1f), fontSize = 14.sp)
                                Text("Latitude", Modifier.weight(1f), fontSize = 14.sp)
                                Text("Longitude", Modifier.weight(1f), fontSize = 14.sp)
                            }
                        }

                        items(rows) { row ->
                            val (timestamp, lat, lon) = row
                            val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(java.util.Date(timestamp))

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(this@SecondActivity, ThirdActivity::class.java).apply {
                                            putExtra("Latitude", lat.toString())
                                            putExtra("Longitude", lon.toString())
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formattedTime, Modifier.weight(1f), fontSize = 12.sp)
                                Text(String.format("%.6f", lat), Modifier.weight(1f), fontSize = 12.sp)
                                Text(String.format("%.6f", lon), Modifier.weight(1f), fontSize = 12.sp)
                            }
                        } // item
                    } // LazyColumn
                } // column
            } // HelloWorldTheme
        } // setContent
    } //


    private fun readFileContents(): String {
        val fileName = "gps_coordinates.csv"
        return try {
            // Open the file from internal storage
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }
        } catch (e: IOException) {
            "Error reading file: ${e.message}"
        }
    }
}