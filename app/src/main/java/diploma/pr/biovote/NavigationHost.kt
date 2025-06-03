package com.example.diplomx

import OnboardingScreen
import VotingListScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import diploma.pr.biovote.ui.auth.LoginScreen
import diploma.pr.biovote.ui.auth.RegistrationScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Register : Screen("register")
    object Login : Screen("login")
    object VotingList : Screen("voting_list")
}

@Composable
fun NavigationHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Register.route)
            })
        }
        composable(Screen.Register.route) {
            RegistrationScreen(onRegistered = {
                navController.navigate(Screen.Login.route)
            })
        }
        composable(Screen.Login.route) {
            LoginScreen(onLoggedIn = {
                navController.navigate(Screen.VotingList.route)
            })
        }
        composable(Screen.VotingList.route) {
            VotingListScreen()
        }
    }
}