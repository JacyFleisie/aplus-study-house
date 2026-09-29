package com.example.blankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.blankapp.navigation.AppNavigation
import com.example.blankapp.ui.theme.AplusStudyHouseTheme
import com.example.blankapp.updater.UpdateStartupChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UpdateStartupChecker.schedule(this)
        setContent {
            AplusStudyHouseTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
