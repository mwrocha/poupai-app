package io.poupai.app.features.dashboard.state

data class MonthData(
    val label: String,
    val income: Double,
    val expense: Double,
)

data class DashboardUiState(
    val userName: String = "",
    val profileImageUrl: String? = null,
    val totalSaved: Double = 0.0,
    val monthlyData: List<MonthData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hideValues: Boolean = false,
)