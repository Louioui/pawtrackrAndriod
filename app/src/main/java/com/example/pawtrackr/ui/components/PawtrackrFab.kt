package com.example.pawtrackr.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor

@Composable
fun PawtrackrFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        text = { Text(label) },
        containerColor = PawtrackrStaticColor.BrandPrimary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation()
    )
}
