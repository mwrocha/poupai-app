package io.poupai.app.core.designsystem.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.poupai.app.core.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Callbacks de navegação entre as 8 telas top-level + logout.
 *
 * Cada tela top-level (Dashboard, Investments, Finances, Transactions,
 * Tags, Goals, Gamification, Profile, Settings) recebe essas lambdas
 * pra alimentar o drawer lateral.
 */
data class TopLevelNavCallbacks(
    val onNavigateToDashboard: () -> Unit,
    val onNavigateToInvestments: () -> Unit,
    val onNavigateToFinances: () -> Unit,
    val onNavigateToTransactions: () -> Unit,
    val onNavigateToTags: () -> Unit,
    val onNavigateToGoals: () -> Unit,
    val onNavigateToGamification: () -> Unit,
    val onNavigateToProfile: () -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onLogout: () -> Unit,
)

/**
 * ViewModel utilitário que provê userName/profileImageUrl para o drawer.
 * Permite que qualquer tela top-level mostre o drawer sem precisar
 * propagar esses dados via parâmetro.
 */
@HiltViewModel
class DrawerHostViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    data class State(
        val userName: String = "",
        val profileImageUrl: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val firstName = preferencesManager.getFirstNameSync()?.ifBlank { null } ?: "Usuário"
            val profileImageUrl = preferencesManager.getProfileImageUrlSync()
            _state.update { it.copy(userName = firstName, profileImageUrl = profileImageUrl) }
        }
    }
}

/**
 * Envolve uma tela top-level com o drawer lateral do Poupaí.
 *
 * Uso:
 * ```
 * PoupaiDrawerScaffold(
 *     selectedRoute = "investments",
 *     nav = topLevelNav,
 * ) { onMenuClick ->
 *     // conteúdo da tela; header chama `onMenuClick` no botão hamburger
 * }
 * ```
 *
 * O drawer só está disponível para as 8 telas top-level (listadas no
 * `drawerMenuItems`). Telas profundas (detalhes, formulários, livro)
 * continuam usando `popBackStack()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoupaiDrawerScaffold(
    selectedRoute: String,
    nav: TopLevelNavCallbacks,
    drawerViewModel: DrawerHostViewModel = hiltViewModel(),
    content: @Composable (onMenuClick: () -> Unit) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerData by drawerViewModel.state.collectAsState()

    val onMenuClick = remember(drawerState, scope) {
        { scope.launch { drawerState.open() }; Unit }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PoupaiDrawerContent(
                userName = drawerData.userName,
                userHandle = "",
                profileImageUrl = drawerData.profileImageUrl,
                selectedRoute = selectedRoute,
                onAvatarClick = {
                    scope.launch { drawerState.close() }
                    if (selectedRoute != "profile") nav.onNavigateToProfile()
                },
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    // Se já estamos nessa rota, não navegamos (evita push duplicado)
                    if (route == selectedRoute) return@PoupaiDrawerContent
                    when (route) {
                        "investments"  -> nav.onNavigateToInvestments()
                        "finances"     -> nav.onNavigateToFinances()
                        "transactions" -> nav.onNavigateToTransactions()
                        "tags"         -> nav.onNavigateToTags()
                        "goals"        -> nav.onNavigateToGoals()
                        "gamification" -> nav.onNavigateToGamification()
                        "profile"      -> nav.onNavigateToProfile()
                        "settings"     -> nav.onNavigateToSettings()
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    nav.onLogout()
                },
            )
        },
        modifier = Modifier,
    ) {
        content(onMenuClick)
    }
}
