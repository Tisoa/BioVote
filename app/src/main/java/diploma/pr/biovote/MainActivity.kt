package com.example.diplomx

import VotingListScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import diploma.pr.biovote.WelcomeScreen
import diploma.pr.biovote.ui.auth.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            setContent {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") { WelcomeScreen(onStartClick = { navController.navigate("login") }) }
                    composable("login") { LoginScreen(onLoggedIn = { navController.navigate("polls") }) }
                    composable("polls") { VotingListScreen() }
                }
            }
        }
    }
}