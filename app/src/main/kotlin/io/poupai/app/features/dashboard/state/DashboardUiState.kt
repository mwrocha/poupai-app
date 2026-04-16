package io.poupai.app.features.dashboard.state

data class DashboardUiState(
    val userName: String = "",
    val totalSaved: Double = 0.0,
    val weeklyData: List<Double> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
