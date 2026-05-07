package io.poupai.app.domain.model

import java.util.Date

data class Goal(
    val id: String,
    val title: String,
    val targetValue: Double,
    val currentValue: Double,
    val deadline: Date?,
    val icon: String,
    val color: String,
) {
    val progress: Float
        get() = if (targetValue > 0) (currentValue / targetValue).toFloat().coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean
        get() = currentValue >= targetValue

    val remaining: Double
        get() = (targetValue - currentValue).coerceAtLeast(0.0)
}