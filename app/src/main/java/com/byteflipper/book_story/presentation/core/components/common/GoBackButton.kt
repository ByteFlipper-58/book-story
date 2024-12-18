package com.byteflipper.book_story.presentation.core.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import com.byteflipper.book_story.R
import com.byteflipper.book_story.domain.util.OnNavigate

/**
 * Go back arrow button.
 * Prevents double or triple clicking while going back action is performed.
 *
 * @param onNavigate [OnNavigate].
 * @param enabled Whether this button is enabled.
 * @param customOnClick Call before going back.
 */
@Composable
fun GoBackButton(
    onNavigate: OnNavigate,
    enabled: Boolean = true,
    customOnClick: () -> Unit = {}
) {
    IconButton(
        icon = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = R.string.go_back_content_desc,
        disableOnClick = true,
        enabled = enabled
    ) {
        customOnClick()
        onNavigate {
            navigateBack()
        }
    }
}