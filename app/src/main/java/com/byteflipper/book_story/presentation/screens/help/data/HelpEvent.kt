package com.byteflipper.book_story.presentation.screens.help.data

import androidx.compose.runtime.Immutable
import com.byteflipper.book_story.presentation.core.navigation.Screen

@Immutable
sealed class HelpEvent {
    data class OnInit(
        val screen: Screen.Help
    ) : HelpEvent()
}