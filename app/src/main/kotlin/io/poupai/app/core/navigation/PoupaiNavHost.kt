package io.poupai.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.poupai.app.core.designsystem.components.TopLevelNavCallbacks
import io.poupai.app.features.auth.ui.LoginScreen
import io.poupai.app.features.auth.ui.WelcomeScreen
import io.poupai.app.features.dashboard.ui.DashboardScreen
import io.poupai.app.features.dividends.ui.DividendsScreen
import io.poupai.app.features.finances.ui.FinancesScreen
import io.poupai.app.features.gamification.ui.GamificationScreen
import io.poupai.app.features.goals.ui.GoalsScreen
import io.poupai.app.features.investmentbook.ui.InvestmentBookScreen
import io.poupai.app.features.incometax.ui.IncomeTaxScreen
import io.poupai.app.features.incometax.ui.TaxClassificationScreen
import io.poupai.app.features.investmentdetail.ui.InvestmentDetailScreen
import io.poupai.app.features.investments.ui.InvestmentsScreen
import io.poupai.app.features.allocation.ui.AllocationScreen
import io.poupai.app.features.investments.ui.RebalanceScreen
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
fun PoupaiNavHost(navController: NavHostController) {

    fun navigateToLogin() {
        navController.navigate(Route.Welcome.route) { popUpTo(0) { inclusive = true } }
    }

    // Navega para uma rota top-level, fazendo singleTop para evitar pilha gigante
    // ao trocar de feature via drawer várias vezes.
    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            // Mantém o Dashboard na base; remove qualquer outra top-level intermediária.
            popUpTo(Route.Dashboard.route) { saveState = false; inclusive = false }
            restoreState = false
        }
    }

    val topLevelNav = TopLevelNavCallbacks(
        onNavigateToDashboard  = { navigateTopLevel(Route.Dashboard.route) },
        onNavigateToInvestments = { navigateTopLevel(Route.Investments.route) },
        onNavigateToFinances    = { navigateTopLevel(Route.Finances.route) },
        onNavigateToTransactions = { navigateTopLevel(Route.Transactions.route) },
        onNavigateToTags        = { navigateTopLevel(Route.Tags.route) },
        onNavigateToGoals       = { navigateTopLevel(Route.Goals.route) },
        onNavigateToGamification = { navigateTopLevel(Route.Gamification.route) },
        onNavigateToProfile     = { navigateTopLevel(Route.Profile.route) },
        onNavigateToSettings    = { navigateTopLevel(Route.Settings.route) },
        onLogout                = { navigateToLogin() },
    )

    NavHost(navController = navController, startDestination = Route.Splash.route) {

        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding.route) {
                        popUpTo(
                            Route.Splash.route
                        ) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(
                            Route.Splash.route
                        ) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Route.Welcome.route) {
                    popUpTo(
                        Route.Onboarding.route
                    ) { inclusive = true }
                }
            })
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
            Route.WelcomeAfterLogin.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            WelcomeAfterLoginScreen(
                userName = backStackEntry.arguments?.getString("userName") ?: "",
                onFinished = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.WelcomeAfterLogin.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(Route.Dashboard.route) {
            DashboardScreen(topLevelNav = topLevelNav)
        }

        composable(Route.Transactions.route) { TransactionsScreen(topLevelNav = topLevelNav) }
        composable(Route.Tags.route) { TagsScreen(topLevelNav = topLevelNav) }
        composable(Route.Finances.route) { FinancesScreen(topLevelNav = topLevelNav) }

        composable(Route.Investments.route) {
            InvestmentsScreen(
                topLevelNav = topLevelNav,
                onNavigateToBook = { navController.navigate(Route.InvestmentBook.route) },
                onNavigateToDividends = { navController.navigate(Route.Dividends.route) },
                onNavigateToRebalance = { navController.navigate(Route.Rebalance.route) },
                onNavigateToAllocation = { navController.navigate(Route.Allocation.route) },
                onNavigateToDetail = { id -> navController.navigate(Route.InvestmentDetail.createRoute(id)) },
                onNavigateToIncomeTax = { navController.navigate(Route.IncomeTax.route) },
            )
        }

        composable(Route.IncomeTax.route) {
            IncomeTaxScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToClassification = { navController.navigate(Route.TaxClassification.route) },
            )
        }

        composable(Route.TaxClassification.route) {
            TaxClassificationScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Allocation.route) {
            AllocationScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Route.InvestmentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            InvestmentDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.InvestmentBook.route) {
            InvestmentBookScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Route.Dividends.route) { DividendsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Route.Rebalance.route) { RebalanceScreen(onNavigateBack = { navController.popBackStack() }) }

        composable(Route.Profile.route) { ProfileScreen(topLevelNav = topLevelNav) }
        composable(Route.Goals.route) { GoalsScreen(topLevelNav = topLevelNav) }
        composable(Route.Settings.route) { SettingsScreen(topLevelNav = topLevelNav) }
        composable(Route.Gamification.route) { GamificationScreen(topLevelNav = topLevelNav) }
    }
}