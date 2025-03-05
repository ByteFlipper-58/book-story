/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class ConsentOption(
    val isVisible: Boolean = true,
    val initialValue: Boolean = false
)

data class ConsentConfig(
    val analyticsStorage: ConsentOption = ConsentOption(),
    val adStorage: ConsentOption = ConsentOption(),
    val adUserData: ConsentOption = ConsentOption(),
    val adPersonalization: ConsentOption = ConsentOption()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentBottomSheet(
    config: ConsentConfig = ConsentConfig(),
    onDismiss: () -> Unit,
    onSaveConsent: (
        analyticsStorage: Boolean,
        adStorage: Boolean,
        adUserData: Boolean,
        adPersonalization: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()

    var analyticsStorage by remember { mutableStateOf(config.analyticsStorage.initialValue) }
    var adStorage by remember { mutableStateOf(config.adStorage.initialValue) }
    var adUserData by remember { mutableStateOf(config.adUserData.initialValue) }
    var adPersonalization by remember { mutableStateOf(config.adPersonalization.initialValue) }

    val handleDismiss = {
        onSaveConsent(false, false, false, false)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        dragHandle = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag handle",
                    modifier = Modifier.alpha(0.5f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Настройки конфиденциальности",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Пожалуйста, выберите, какие данные вы разрешаете нам собирать для улучшения работы приложения:",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (config.analyticsStorage.isVisible) {
                ConsentCheckboxItem(
                    title = "Аналитика",
                    description = "Сбор данных об использовании приложения для улучшения производительности",
                    checked = analyticsStorage,
                    onCheckedChange = { analyticsStorage = it }
                )
            }

            if (config.adStorage.isVisible) {
                ConsentCheckboxItem(
                    title = "Реклама",
                    description = "Хранение рекламных данных для показа релевантной рекламы",
                    checked = adStorage,
                    onCheckedChange = { adStorage = it }
                )
            }

            if (config.adUserData.isVisible) {
                ConsentCheckboxItem(
                    title = "Пользовательские данные",
                    description = "Сбор данных для улучшения рекламного опыта",
                    checked = adUserData,
                    onCheckedChange = { adUserData = it }
                )
            }

            if (config.adPersonalization.isVisible) {
                ConsentCheckboxItem(
                    title = "Персонализация рекламы",
                    description = "Использование данных для показа персонализированной рекламы",
                    checked = adPersonalization,
                    onCheckedChange = { adPersonalization = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = {
                    onSaveConsent(
                        if (config.analyticsStorage.isVisible) analyticsStorage else false,
                        if (config.adStorage.isVisible) adStorage else false,
                        if (config.adUserData.isVisible) adUserData else false,
                        if (config.adPersonalization.isVisible) adPersonalization else false
                    )
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить настройки")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConsentCheckboxItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConsentBottomSheet() {
    ConsentBottomSheet(
        config = ConsentConfig(),
        onDismiss = {},
        onSaveConsent = { _, _, _, _ -> }
    )
}

/*
* // Пример 1: Все опции видимы
ConsentBottomSheet(
    config = ConsentConfig(), // Дефолтная конфигурация - все видимы
    onDismiss = { /* Handle dismissal */ },
    onSaveConsent = { analytics, ad, userData, personalization ->
        Firebase.analytics.setConsent {
            analyticsStorage(if (analytics) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adStorage(if (ad) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adUserData(if (userData) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adPersonalization(if (personalization) ConsentStatus.GRANTED else ConsentStatus.DENIED)
        }
    }
)

// Пример 2: Скрыть некоторые опции
ConsentBottomSheet(
    config = ConsentConfig(
        analyticsStorage = ConsentOption(isVisible = true),
        adStorage = ConsentOption(isVisible = true),
        adUserData = ConsentOption(isVisible = false), // Скрыть
        adPersonalization = ConsentOption(isVisible = false) // Скрыть
    ),
    onDismiss = { /* Handle dismissal */ },
    onSaveConsent = { analytics, ad, userData, personalization ->
        // userData и personalization будут всегда false
        Firebase.analytics.setConsent {
            analyticsStorage(if (analytics) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adStorage(if (ad) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adUserData(ConsentStatus.DENIED)
            adPersonalization(ConsentStatus.DENIED)
        }
    }
)

// Пример 3: Установить начальные значения
ConsentBottomSheet(
    config = ConsentConfig(
        analyticsStorage = ConsentOption(isVisible = true, initialValue = true),
        adStorage = ConsentOption(isVisible = true, initialValue = false),
        adUserData = ConsentOption(isVisible = true, initialValue = true),
        adPersonalization = ConsentOption(isVisible = true, initialValue = false)
    ),
    onDismiss = { /* Handle dismissal */ },
    onSaveConsent = { analytics, ad, userData, personalization ->
        Firebase.analytics.setConsent {
            analyticsStorage(if (analytics) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adStorage(if (ad) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adUserData(if (userData) ConsentStatus.GRANTED else ConsentStatus.DENIED)
            adPersonalization(if (personalization) ConsentStatus.GRANTED else ConsentStatus.DENIED)
        }
    }
)

/////////////////////////////

var showConsentSheet by remember { mutableStateOf(false) }

ElevatedButton(
                onClick = { showConsentSheet = true }, // Открываем модальное окно
                shape = MaterialTheme.shapes.medium,
            )


if (showConsentSheet) {
        ConsentBottomSheet(
            config = ConsentConfig(
                analyticsStorage = ConsentOption(isVisible = true, initialValue = true),
                adStorage = ConsentOption(isVisible = true, initialValue = true),
                adUserData = ConsentOption(isVisible = true, initialValue = true),
                adPersonalization = ConsentOption(isVisible = true, initialValue = true)
            ),
            onDismiss = { showConsentSheet = false },
            onSaveConsent = { analytics, ad, userData, personalization ->
                Firebase.analytics.setConsent(
                    mapOf(
                        FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to
                                if (analytics) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                        FirebaseAnalytics.ConsentType.AD_STORAGE to
                                if (ad) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                        FirebaseAnalytics.ConsentType.AD_USER_DATA to
                                if (userData) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                        FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to
                                if (personalization) ConsentStatus.GRANTED else ConsentStatus.DENIED
                    )
                )
                showConsentSheet = false
            }
        )
    }

 */