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

//v6
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.helloworld.room.AppDatabase
import com.example.helloworld.room.CoordinatesEntity

class SecondActivity : AppCompatActivity() {
    private val TAG = "btaSecondActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.")

        // dont need anymore
//        val fileContentsState = mutableStateOf("Loading...")
//        fileContentsState.value = readFileContents()

        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            HelloWorldTheme {
                // dont need anymore
//                val fileContents = fileContentsState.value
//                val rows = fileContents.trim().lines().mapNotNull { ... }
                val db = AppDatabase.getDatabase(this)
                val coordinatesList = remember { mutableStateOf(listOf<CoordinatesEntity>()) }

                // Refreshes room information everytime the activity is resumed
                // To be able to go back and forth from third to second activity
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val updatedData = db.coordinatesDao().getAll()
                                withContext(Dispatchers.Main) {
                                    coordinatesList.value = updatedData
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
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
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Date/Time", Modifier.weight(1f), fontSize = 14.sp)
                                Text("Latitude", Modifier.weight(1f), fontSize = 14.sp)
                                Text("Longitude", Modifier.weight(1f), fontSize = 14.sp)
                            }
                        }

                        items(coordinatesList.value) { coord ->
                            val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(java.util.Date(coord.timestamp))

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(this@SecondActivity, ThirdActivity::class.java).apply {
                                            putExtra("Latitude", coord.latitude.toString())
                                            putExtra("Longitude", coord.longitude.toString())
                                            putExtra("Timestamp", coord.timestamp.toString())
                                        }
                                        startActivity(intent)
                                        // finish() // Commented out after adding coordinate edit screen
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formattedTime, Modifier.weight(1f), fontSize = 12.sp)
                                Text(String.format("%.6f", coord.latitude), Modifier.weight(1f), fontSize = 12.sp)
                                Text(String.format("%.6f", coord.longitude), Modifier.weight(1f), fontSize = 12.sp)
                            }
                        }
                    }
                }
                // end of edits
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