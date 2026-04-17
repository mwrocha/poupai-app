package io.poupai.app.data.remote.dto

// ─── Wrapper que bate com o formato do backend ───
// {"success":true,"message":"...","data":{...}}
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val birthDate: String?,
    val profileImageUrl: String?,
)

data class UserDto(
    val id: String?,
    val email: String?,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val birthDate: String?,
    val profileImageUrl: String?,
    val token: String?,
)

data class TransactionDto(
    val id: String,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val tagId: String?,
)

data class InvestmentDto(
    val id: String,
    val name: String,
    val type: String,
    val currentValue: Double,
    val investedValue: Double,
    val profitability: Double,
)

data class FinanceSummaryDto(
    val incomeHistory: List<Double>,
    val expenseHistory: List<Double>,
    val profitHistory: List<Double>,
)