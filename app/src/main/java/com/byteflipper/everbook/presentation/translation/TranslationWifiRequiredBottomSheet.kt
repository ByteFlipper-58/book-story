package com.byteflipper.everbook.presentation.translation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.modal_bottom_sheet.ModalBottomSheet
import com.byteflipper.everbook.presentation.core.components.settings.SwitchWithTitle

@Composable
fun TranslationWifiRequiredBottomSheet(
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            StyledText(
                text = stringResource(id = R.string.translation_wifi_required_title),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge
            )
            StyledText(
                text = stringResource(id = R.string.translation_wifi_required_message),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            SwitchWithTitle(
                selected = wifiOnly,
                title = stringResource(id = R.string.translation_wifi_only_option),
                description = stringResource(id = R.string.translation_wifi_only_option_desc),
                onClick = { onWifiOnlyChange(!wifiOnly) }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    StyledText(text = stringResource(id = R.string.cancel))
                }
                Button(enabled = !wifiOnly, onClick = onContinue) {
                    StyledText(text = stringResource(id = R.string.translation_download))
                }
            }
        }
    }
}
