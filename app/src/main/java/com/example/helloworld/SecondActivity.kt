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
                Column( // center the buttons
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues()), // Add padding for system bars
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // button to go back to MainActivity
                    Button(
                        onClick = {
                            val intent = Intent(this@SecondActivity, MainActivity::class.java)
                            startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF003eff), // background blue color
                            contentColor = Color.White // text color
                        )
                    ) {
                        Text("Go to Main Activity")
                    }

                    // button to navigate to ThirdActivity
                    Button(onClick = {
                        val intent = Intent(this@SecondActivity, ThirdActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("Go to Third Activity")
                    }

                    // textview right below buttons
                    Text(text = "GPS Coordinates File Contents:")
                    Text(
                        text = fileContentsState.value,
                        modifier = Modifier.fillMaxSize()
                    )
                } // Column
            } // HelloWorldTheme
        }
    }

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