package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.helloworld.room.AppDatabase
import com.example.helloworld.room.CoordinatesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : ComponentActivity() {
    private val TAG = "btaThirdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val timestamp = intent.getStringExtra("Timestamp") ?: ""
        val lat = intent.getStringExtra("Latitude") ?: ""
        val lon = intent.getStringExtra("Longitude") ?: ""

        setContent {
            CoordinateEditorScreen(timestamp, lat, lon)
        }
    }
}

@Composable
fun CoordinateEditorScreen(initialTimestamp: String, initialLat: String, initialLon: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    var timestamp by remember { mutableStateOf(initialTimestamp) }
    var latitude by remember { mutableStateOf(initialLat) }
    var longitude by remember { mutableStateOf(initialLon) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Edit Coordinate", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = timestamp,
            onValueChange = { timestamp = it },
            label = { Text("Timestamp") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            db.coordinatesDao().deleteWithTimestamp(timestamp.toLong())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                // context.startActivity(Intent(context, SecondActivity::class.java))
                                // Finish instance of third activity once second activity opened again
                                (context as? ComponentActivity)?.finish()
                            }
                        } catch (e: Exception) {
                            Log.e("ThirdActivity", "Delete error: ${e.message}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        val updated = CoordinatesEntity(
                            timestamp = timestamp.toLong(),
                            latitude = latitude.toDouble(),
                            longitude = longitude.toDouble()
                        )
                        db.coordinatesDao().updateCoordinate(updated)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                            // context.startActivity(Intent(context, SecondActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    } catch (e: Exception) {
                        Log.e("ThirdActivity", "Update error: ${e.message}")
                    }
                }
            }) {
                Text("Update")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            // context.startActivity(Intent(context, SecondActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }) {
            Text("Back to List")
        }
    }
}
