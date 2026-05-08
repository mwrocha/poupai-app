package io.poupai.app.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun EyeToggleIcon(
    hideValues: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (hideValues) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (hideValues) "Mostrar valores" else "Ocultar valores",
            tint = tint,
        )
    }
}