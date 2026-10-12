package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.PennywiseApp
import com.example.ui.PennywiseViewModel
import com.example.util.AppCheckUtils

class MainActivity : ComponentActivity() {
    private val viewModel: PennywiseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with reCAPTCHA Enterprise and Debug providers
        AppCheckUtils.configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            PennywiseApp(viewModel = viewModel)
        }
    }
}
