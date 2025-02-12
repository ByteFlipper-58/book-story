package com.byteflipper.everbook.presentation.about

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.domain.about.Badge
import com.byteflipper.everbook.presentation.core.components.common.IconButton

@Composable
fun AboutBadgeItem(
    badge: Badge,
    onClick: () -> Unit
) {
    if (badge.imageVector == null && badge.drawable != null) {
        IconButton(
            modifier = Modifier.size(22.dp),
            icon = badge.drawable,
            contentDescription = badge.contentDescription,
            disableOnClick = false,
            color = MaterialTheme.colorScheme.primary
        ) {
            onClick()
        }
    } else if (badge.imageVector != null && badge.drawable == null) {
        IconButton(
            modifier = Modifier.size(22.dp),
            icon = badge.imageVector,
            contentDescription = badge.contentDescription,
            disableOnClick = false,
            color = MaterialTheme.colorScheme.primary
        ) {
            onClick()
        }
    }
}