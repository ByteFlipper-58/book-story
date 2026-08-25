/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.settings.reader.tts.components

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.reader.tts.TtsVoice
import com.byteflipper.everbook.presentation.core.components.common.IconButton
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.components.dialog.Dialog
import com.byteflipper.everbook.presentation.settings.reader.tts.TtsVoicesModel
import com.byteflipper.everbook.ui.main.MainEvent
import com.byteflipper.everbook.ui.main.MainModel

// The speech settings screen has no public Settings constant; this is the action the framework
// screen declares. Devices without it fall back to the engine's voice-data installer.
private const val SPEECH_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"

@Composable
fun TtsVoiceOption() {
    val mainModel = hiltViewModel<MainModel>()
    val voicesModel = hiltViewModel<TtsVoicesModel>()
    val state = mainModel.state.collectAsStateWithLifecycle()
    val voices = voicesModel.voices.collectAsStateWithLifecycle()

    var dialogShown by remember { mutableStateOf(false) }

    // Voice data can be installed while the system screen is open, so the list is re-read on return.
    val systemSpeechSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        voicesModel.loadVoices()
    }
    val openSystemSpeechSettings: () -> Unit = {
        runCatching {
            systemSpeechSettings.launch(Intent(SPEECH_SETTINGS_ACTION))
        }.onFailure {
            runCatching {
                systemSpeechSettings.launch(
                    Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        voicesModel.loadVoices()
    }

    val selectedVoice = remember(voices.value, state.value.ttsVoice) {
        voices.value.find { it.id == state.value.ttsVoice }
    }

    val summary = when {
        voices.value.isEmpty() -> stringResource(id = R.string.tts_voices_empty)
        selectedVoice != null -> selectedVoice.displayName
        else -> stringResource(id = R.string.tts_voice_system_default)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (voices.value.isEmpty()) openSystemSpeechSettings() else dialogShown = true
            }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            StyledText(
                text = stringResource(id = R.string.tts_voice_option),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            StyledText(
                text = summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (selectedVoice?.requiresNetwork == true) {
                StyledText(
                    text = stringResource(id = R.string.tts_voice_network_required),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))
        IconButton(
            icon = R.drawable.ic_open_in_new_rounded_24px,
            contentDescription = R.string.tts_open_system_settings,
            disableOnClick = false,
            onClick = openSystemSpeechSettings
        )
    }

    if (dialogShown) {
        TtsVoiceDialog(
            voices = voices.value,
            selectedVoiceId = state.value.ttsVoice,
            onDismiss = { dialogShown = false },
            onSelect = {
                dialogShown = false
                mainModel.onEvent(MainEvent.OnChangeTtsVoice(it))
            }
        )
    }
}

@Composable
private fun TtsVoiceDialog(
    voices: List<TtsVoice>,
    selectedVoiceId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var pendingVoiceId by remember(selectedVoiceId) { mutableStateOf(selectedVoiceId) }

    Dialog(
        title = stringResource(id = R.string.tts_voice_option),
        description = null,
        actionEnabled = true,
        onDismiss = onDismiss,
        onAction = { onSelect(pendingVoiceId) },
        withContent = true,
        items = {
            item {
                TtsVoiceDialogItem(
                    selected = pendingVoiceId.isBlank(),
                    title = stringResource(id = R.string.tts_voice_system_default),
                    description = null,
                    onClick = { pendingVoiceId = "" }
                )
            }

            items(voices, key = { it.id }) { voice ->
                TtsVoiceDialogItem(
                    selected = voice.id == pendingVoiceId,
                    title = voice.displayName,
                    // Engine ids look like "ru-ru-x-ruf-local"; several voices share a locale and
                    // therefore the same display name, so the id is what tells them apart.
                    description = voice.id.takeIf { it.isNotBlank() },
                    networkRequired = voice.requiresNetwork,
                    onClick = { pendingVoiceId = voice.id }
                )
            }
        }
    )
}

@Composable
private fun TtsVoiceDialogItem(
    selected: Boolean,
    title: String,
    description: String?,
    networkRequired: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !selected) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            modifier = Modifier.size(24.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            StyledText(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            if (description != null) {
                StyledText(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
            if (networkRequired) {
                StyledText(
                    text = stringResource(id = R.string.tts_voice_network_required),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
