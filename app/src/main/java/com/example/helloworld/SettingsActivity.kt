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
import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            HelloWorldTheme {
                val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                val userId = sharedPrefs.getString("userIdentifier", "No ID saved")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                        .padding(top = 24.dp),  // handles notch/status bar
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues()),  // handles notch/status bar
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    val userId = sharedPrefs.getString("userIdentifier", "No ID saved")
                    Text("User ID: $userId")
                    Spacer(modifier = Modifier.height(8.dp))
                    AppSettingsButton()
                    SettingsButton()
                }
            }
        }
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