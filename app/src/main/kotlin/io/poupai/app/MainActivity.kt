package io.poupai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import io.poupai.app.core.navigation.PoupaiNavHost
import io.poupai.app.core.navigation.Route
import io.poupai.app.core.network.SessionManager
import io.poupai.app.core.theme.PoupaiTheme
import io.poupai.app.core.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by preferencesManager.appTheme.collectAsState(initial = "system")
            val systemDark = isSystemInDarkTheme()

            val isDark = when (theme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            PoupaiTheme(darkTheme = isDark) {
                val navController = rememberNavController()

                // Observa token expirado e redireciona para o login
                LaunchedEffect(Unit) {
                    sessionManager.sessionExpiredEvent.collect {
                        navController.navigate(Route.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                PoupaiNavHost(navController = navController)
            }
        }
    }
}