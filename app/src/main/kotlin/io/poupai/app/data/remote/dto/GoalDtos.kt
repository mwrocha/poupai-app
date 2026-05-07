package io.poupai.app.data.remote.dto

data class GoalDto(
    val id: String,
    val title: String,
    val targetValue: Double,
    val currentValue: Double,
    val deadline: String?,
    val icon: String,
    val color: String,
)

data class CreateGoalRequest(
    val title: String,
    val targetValue: Double,
    val currentValue: Double,
    val deadline: String?,
    val icon: String,
    val color: String,
)

data class UpdateGoalProgressRequest(
    val currentValue: Double,
)