package com.example.blankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.blankapp.navigation.AppNavigation
import com.example.blankapp.ui.theme.AplusStudyHouseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplusStudyHouseTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
