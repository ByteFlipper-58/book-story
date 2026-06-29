/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.start

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
fun LazyListScope.StartSettingsLayoutNotifications() {
    item {
        NotificationsContent()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationsContent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        val granted = permissionState.status.isGranted

        NotificationsLayout(
            granted = granted,
            onRequest = { permissionState.launchPermissionRequest() }
        )
    } else {
        NotificationsLayout(
            granted = true,
            onRequest = {}
        )
    }
}

@Composable
private fun NotificationsLayout(
    granted: Boolean,
    onRequest: () -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))

    NotificationStatusIcon(granted = granted)

    Spacer(modifier = Modifier.height(16.dp))

    AnimatedContent(
        targetState = granted,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.92f))
                .togetherWith(fadeOut())
        },
        label = "notification_button"
    ) { isGranted ->
        if (isGranted) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                text = stringResource(id = R.string.start_notifications_granted),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        } else {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                onClick = onRequest,
                shape = MaterialTheme.shapes.medium
            ) {
                StyledText(text = stringResource(id = R.string.start_notifications_grant))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    NotificationUsageList()

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun NotificationStatusIcon(granted: Boolean) {
    AnimatedContent(
        targetState = granted,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.8f))
                .togetherWith(fadeOut())
        },
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
        label = "notification_icon"
    ) { isGranted ->
        Icon(
            imageVector = if (isGranted) {
                Icons.Outlined.NotificationsActive
            } else {
                Icons.Outlined.NotificationsOff
            },
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = if (isGranted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun NotificationUsageList() {
    Text(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        text = stringResource(id = R.string.start_permissions_notifications_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(8.dp))

    NotificationUsageItem(
        icon = Icons.Outlined.SystemUpdate,
        text = stringResource(id = R.string.start_notifications_item_updates)
    )
    NotificationUsageItem(
        icon = Icons.Outlined.Translate,
        text = stringResource(id = R.string.start_notifications_item_translation)
    )
    NotificationUsageItem(
        icon = Icons.Outlined.Language,
        text = stringResource(id = R.string.start_notifications_item_models)
    )
}

@Composable
private fun NotificationUsageItem(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
