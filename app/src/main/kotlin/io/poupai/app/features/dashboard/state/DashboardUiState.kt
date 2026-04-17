package io.poupai.app.features.dashboard.state

data class DashboardUiState(
    val userName: String = "",
    val profileImageUrl: String? = null,
    val totalSaved: Double = 0.0,
    val weeklyData: List<Double> = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)