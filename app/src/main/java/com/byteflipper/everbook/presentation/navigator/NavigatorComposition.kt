package com.byteflipper.everbook.presentation.navigator

import androidx.compose.runtime.compositionLocalOf
import com.byteflipper.everbook.domain.navigator.Navigator

val LocalNavigator = compositionLocalOf<Navigator> { error("Navigator was not passed.") }