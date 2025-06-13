package diploma.pr.biovote

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import diploma.pr.biovote.ui.voting.PollDetailScreen
import diploma.pr.biovote.ui.voting.VotingListScreen

// Оголошуємо маршрути
sealed class Screen(val route: String) {
    object Onboarding  : Screen("onboarding")
    object Register    : Screen("register")
    object VotingList  : Screen("voting_list")
    object PollDetail  : Screen("poll_detail/{pollId}")
}

@Composable
fun NavigationHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Onboarding.route) {
        // Екран привітання (онбординг)
        composable(Screen.Onboarding.route) {
            OnboardingScreen {
                // Після натискання кнопки - переходимо до реєстрації
                navController.navigate(Screen.Register.route)
            }
        }
        // Екран реєстрації
        composable(Screen.Register.route) {
            RegistrationScreen(onSuccess = {
                // Після успішної реєстрації - переходимо до списку голосувань
                navController.navigate(Screen.VotingList.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = false }
                }
            })
        }
        // Екран списку голосувань
        composable(Screen.VotingList.route) {
            VotingListScreen(
                onPollSelected = { pollId ->
                    // Натиснули на голосування - переходимо до детального перегляду
                    navController.navigate("poll_detail/$pollId")
                }
            )
        }
        // Детальний екран голосування з передачею параметра pollId
        composable(
            route = "poll_detail/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.LongType })
        ) { backStackEntry ->
            val pollId = backStackEntry.arguments?.getLong("pollId") ?: 0L
            PollDetailScreen(pollId = pollId)
        }
    }
}