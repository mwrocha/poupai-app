package io.poupai.app.core.navigation

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Welcome : Route("welcome")
    data object Login : Route("login")
    data object RegisterCredentials : Route("register/credentials")
    data object RegisterProfile : Route("register/profile")

    data object WelcomeAfterLogin : Route("welcome_after_login/{userName}") {
        fun createRoute(userName: String) = "welcome_after_login/$userName"
    }

    data object Dashboard : Route("dashboard")
    data object Transactions : Route("transactions")

    data object Tags : Route("tags")
    data object Finances : Route("finances")
    data object Investments : Route("investments")
    data object InvestmentBook : Route("investments/book")
    data object Dividends : Route("investments/dividends")
    data object Rebalance : Route("investments/rebalance")
    data object Profile : Route("profile")
    data object Goals : Route("goals")
    data object Settings : Route("settings")
    data object Gamification : Route("gamification")
}