package io.poupai.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.poupai.app.features.auth.ui.LoginScreen
import io.poupai.app.features.auth.ui.WelcomeScreen
import io.poupai.app.features.dashboard.ui.DashboardScreen
import io.poupai.app.features.finances.ui.FinancesScreen
import io.poupai.app.features.gamification.ui.GamificationScreen
import io.poupai.app.features.goals.ui.GoalsScreen
import io.poupai.app.features.investments.ui.InvestmentsScreen
import io.poupai.app.features.onboarding.ui.OnboardingScreen
import io.poupai.app.features.profile.ui.ProfileScreen
import io.poupai.app.features.register.ui.RegisterCredentialsScreen
import io.poupai.app.features.register.ui.RegisterProfileScreen
import io.poupai.app.features.settings.ui.SettingsScreen
import io.poupai.app.features.splash.ui.SplashScreen
import io.poupai.app.features.splash.ui.WelcomeAfterLoginScreen
import io.poupai.app.features.tags.ui.TagsScreen
import io.poupai.app.features.transactions.ui.TransactionsScreen

@Composable
fun PoupaiNavHost() {
    val navController = rememberNavController()

    fun navigateToLogin() {
        navController.navigate(Route.Welcome.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Route.Splash.route) {

        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Welcome.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Route.Login.route) },
                onNavigateToRegister = { navController.navigate(Route.RegisterCredentials.route) },
            )
        }

        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = { userName ->
                    navController.navigate(Route.WelcomeAfterLogin.createRoute(userName)) {
                        popUpTo(Route.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Route.RegisterCredentials.route) {
            RegisterCredentialsScreen(
                onNext = { navController.navigate(Route.RegisterProfile.route) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Route.RegisterProfile.route) {
            RegisterProfileScreen(
                onFinish = { userName ->
                    navController.navigate(Route.WelcomeAfterLogin.createRoute(userName)) {
                        popUpTo(Route.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.WelcomeAfterLogin.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            WelcomeAfterLoginScreen(
                userName = userName,
                onFinished = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.WelcomeAfterLogin.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = { navController.navigate(Route.Transactions.route) },
                onNavigateToTags = { navController.navigate(Route.Tags.route) },
                onNavigateToFinances = { navController.navigate(Route.Finances.route) },
                onNavigateToInvestments = { navController.navigate(Route.Investments.route) },
                onNavigateToGoals = { navController.navigate(Route.Goals.route) },
                onNavigateToProfile = { navController.navigate(Route.Profile.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) },
                onNavigateToGamification = { navController.navigate(Route.Gamification.route) },
                onLogout = { navigateToLogin() },
            )
        }

        composable(Route.Transactions.route) {
            TransactionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Tags.route) {
            TagsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Finances.route) {
            FinancesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Investments.route) {
            InvestmentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = { navigateToLogin() },
            )
        }

        composable(Route.Goals.route) {
            GoalsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Gamification.route) {
            GamificationScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}