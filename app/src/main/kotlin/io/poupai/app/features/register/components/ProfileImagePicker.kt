package io.poupai.app.features.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProfileImagePicker(
    currentImagePath: String?,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { /* TODO: abrir seletor de imagem */ },
        contentAlignment = Alignment.Center,
    ) {
        if (currentImagePath != null) {
            Icon(Icons.Default.Person, "Foto selecionada",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary)
        } else {
            Icon(Icons.Default.CameraAlt, "Selecionar foto",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
