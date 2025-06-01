package com.example.helloworld

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// v2 added imports
import com.example.helloworld.ui.theme.HelloWorldTheme
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.util.Log
import androidx.compose.ui.unit.sp

class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.")

        enableEdgeToEdge()
        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            HelloWorldTheme {
                Column( // set in the center of screen
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues()), // Add padding for system bars
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // GPS coordinate that was selected from secondactivity
                    val latitude = intent.getStringExtra("Latitude")
                    val longitude = intent.getStringExtra("Longitude")
                    Text("Latitude: $latitude", fontSize = 16.sp)
                    Text("Longitude: $longitude", fontSize = 16.sp)

                    // button to go back to SecondActivity
                    Button(onClick = {
                        val intent = Intent(this@ThirdActivity, SecondActivity::class.java)
                        startActivity(intent)
                        finish()
                    }) {
                        Text("Back")
                    }
                } // Column
            } // HelloWorldTheme
        }
    }
}