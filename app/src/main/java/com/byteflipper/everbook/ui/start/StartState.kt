package com.byteflipper.everbook.ui.start

import androidx.compose.runtime.Immutable

@Immutable
data class StartState(
    val storagePermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false
)