package io.poupai.app.data.mapper

import io.poupai.app.data.remote.dto.GoalDto
import io.poupai.app.domain.model.Goal
import java.text.SimpleDateFormat
import java.util.Locale

private val goalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

fun GoalDto.toDomain(): Goal = Goal(
    id = id,
    title = title,
    targetValue = targetValue,
    currentValue = currentValue,
    deadline = deadline?.let {
        try { goalDateFormat.parse(it) } catch (e: Exception) { null }
    },
    icon = icon,
    color = color,
)