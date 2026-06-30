/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.core.util

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.withTimeoutOrNull

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    return this.combinedClickable(
        indication = null,
        interactionSource = null,
        enabled = enabled,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick
    )
}

/**
 * Single/double tap detector that wins over an enclosing text [SelectionContainer].
 *
 * Compose foundation (1.7+) added double-tap-to-select-word to selectable text. That gesture lives
 * *inside* [androidx.compose.foundation.text.BasicText] — i.e. a descendant of this modifier — and
 * consumes the double tap on the Main pass before an outer [combinedClickable] ever sees it. To
 * reclaim the double tap we observe pointer events on the [PointerEventPass.Initial] pass (dispatched
 * ancestor → descendant) and consume the second tap there, so selection's inner handler never gets
 * an unconsumed event. A long press / drag is left untouched so text selection and scrolling keep
 * working.
 */
@Composable
fun Modifier.doubleTapPriorityGestures(
    enabled: Boolean,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit
): Modifier {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)

    return if (!enabled) this else this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

            // A tap is a press released before the long-press timeout; otherwise it is a long
            // press / drag and we bail so the SelectionContainer (and scroll) can handle it.
            val firstUp = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation(PointerEventPass.Initial)
            }
            if (firstUp == null) return@awaitEachGesture

            // It's a real tap (not a drag): consume the release on the Initial pass so the ancestor
            // clickable (the reader's tap-to-toggle-menu) doesn't ALSO fire — otherwise both handlers
            // run and the menu toggle cancels itself out, which reads as the reader being unresponsive.
            // The first down is deliberately left unconsumed so scrolling keeps working.
            firstUp.consume()

            val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            }
            if (secondDown == null) {
                currentOnTap()
            } else {
                // Consume on the Initial pass so the text-selection gesture never starts.
                secondDown.consume()
                withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation(PointerEventPass.Initial)
                }?.consume()
                currentOnDoubleTap()
            }
        }
    }
}