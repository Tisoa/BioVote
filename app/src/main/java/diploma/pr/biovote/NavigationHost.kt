package diploma.pr.biovote

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import diploma.pr.biovote.ui.auth.RegistrationScreen
import diploma.pr.biovote.ui.voting.PollDetailScreen
import diploma.pr.biovote.ui.voting.VotingListScreen

@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    val ctx = LocalContext.current
    val prefs = ctx.getSharedPreferences("biovote_prefs", Context.MODE_PRIVATE)

    val start = "registration"  // Always start with registration screen

    NavHost(navController, startDestination = start) {
        composable("registration") {
            RegistrationScreen {
                navController.navigate("onboarding") {
                    popUpTo("registration") { inclusive = true }
                }
            }
        }

        composable("onboarding") {
            OnboardingScreen {
                prefs.edit().putBoolean("onboarding_finished", true).apply()
                navController.navigate("poll_list") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
        }

        composable("poll_list") {
            VotingListScreen(
                onPollSelected = { pollId ->
                    navController.navigate("poll_detail/$pollId")
                }
            )
        }

        composable(
            "poll_detail/{pollId}",
            arguments = listOf(navArgument("pollId") {
                type = NavType.LongType
            })
        ) { back ->
            PollDetailScreen(
                pollId = back.arguments!!.getLong("pollId"),
                onBack = { navController.popBackStack() }
            )
        }
    }
}