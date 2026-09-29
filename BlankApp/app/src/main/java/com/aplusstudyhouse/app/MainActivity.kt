package com.aplusstudyhouse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.aplusstudyhouse.app.navigation.AppNavigation
import com.aplusstudyhouse.app.ui.theme.AplusStudyHouseTheme
import com.aplusstudyhouse.app.updater.UpdateStartupChecker
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
