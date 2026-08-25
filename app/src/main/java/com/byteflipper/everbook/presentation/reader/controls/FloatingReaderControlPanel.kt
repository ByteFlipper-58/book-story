/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader.controls

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.presentation.core.components.common.AnimatedVisibility
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

const val READER_PANEL_BOTTOM_LEFT = "BOTTOM_LEFT"
const val READER_PANEL_BOTTOM_CENTER = "BOTTOM_CENTER"
const val READER_PANEL_BOTTOM_RIGHT = "BOTTOM_RIGHT"

private const val COLLAPSE_DELAY_MS = 3000L
private val TRACK_HEIGHT = 28.dp
private val TRACK_INSET = 14.dp

/**
 * Auto-collapse timer of a floating reader panel. The panel shrinks to a chip once the user stops
 * interacting with it, and every interaction restarts the countdown.
 */
@Stable
class ReaderPanelCollapseState internal constructor(
    private val collapsed: MutableState<Boolean>,
    private val lastInteractionAt: MutableState<Long>
) {
    val isCollapsed: Boolean get() = collapsed.value

    internal val interactionMark: Long get() = lastInteractionAt.value

    internal fun collapse() {
        collapsed.value = true
    }

    /** Expands the panel and restarts the countdown. */
    fun expand() {
        collapsed.value = false
        touch()
    }

    /** Restarts the countdown without changing the panel size. */
    fun touch() {
        lastInteractionAt.value = System.currentTimeMillis()
    }
}

/**
 * [active] drives the countdown: the panel collapses while it is on and expands again once the
 * session ends. [resetKey] drops the remembered size when the reader switches to another book.
 */
@Composable
fun rememberReaderPanelCollapseState(
    active: Boolean,
    resetKey: Any?,
    collapseDelayMillis: Long = COLLAPSE_DELAY_MS
): ReaderPanelCollapseState {
    val collapsed = rememberSaveable(resetKey) { mutableStateOf(false) }
    val lastInteractionAt = remember(resetKey) { mutableLongStateOf(System.currentTimeMillis()) }
    val state = remember(collapsed, lastInteractionAt) {
        ReaderPanelCollapseState(collapsed, lastInteractionAt)
    }

    LaunchedEffect(active, state.isCollapsed, state.interactionMark) {
        if (active && !state.isCollapsed) {
            delay(collapseDelayMillis)
            state.collapse()
        }
    }

    LaunchedEffect(active) {
        if (!active) state.expand()
    }

    return state
}

/**
 * Card floating above the reader that holds the controls of a running session (auto-scroll or
 * read aloud). It shows [expandedContent] until the auto-collapse timer fires, then shrinks into a
 * chip showing [collapsedContent]; tapping the chip expands it again.
 *
 * Place it inside a `Box` and pass the alignment/insets through [modifier].
 */
@Composable
fun FloatingReaderControlPanel(
    visible: Boolean,
    collapseState: ReaderPanelCollapseState,
    alignment: String,
    collapsedWidthFraction: Float,
    expandedHeight: Dp,
    collapsedAlpha: Float,
    modifier: Modifier = Modifier,
    collapsedContent: @Composable RowScope.() -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        val isCollapsed = collapseState.isCollapsed
        val sizeSpec = spring<Dp>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
        val fractionSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val panelHeight by animateDpAsState(
            targetValue = if (isCollapsed) 40.dp else expandedHeight,
            animationSpec = sizeSpec
        )
        val panelWidthFraction by animateFloatAsState(
            targetValue = if (isCollapsed) collapsedWidthFraction else 1f,
            animationSpec = fractionSpec
        )
        val panelCorner by animateDpAsState(
            targetValue = if (isCollapsed) 20.dp else 28.dp,
            animationSpec = sizeSpec
        )
        // Fading in the chip is delayed until the shrink animation is over, otherwise the panel
        // turns translucent while it is still wide.
        val panelAlpha by animateFloatAsState(
            targetValue = if (isCollapsed) collapsedAlpha else 1f,
            animationSpec = if (isCollapsed) {
                tween(durationMillis = 300, delayMillis = 300)
            } else {
                tween(durationMillis = 150)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentAlignment = when (alignment) {
                READER_PANEL_BOTTOM_LEFT -> Alignment.BottomStart
                READER_PANEL_BOTTOM_CENTER -> Alignment.BottomCenter
                else -> Alignment.BottomEnd
            }
        ) {
            Card(
                modifier = Modifier
                    .graphicsLayer { alpha = panelAlpha }
                    .fillMaxWidth(panelWidthFraction)
                    .height(panelHeight)
                    .noRippleClickable { collapseState.expand() },
                shape = RoundedCornerShape(panelCorner),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (isCollapsed) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        content = collapsedContent
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        content = expandedContent
                    )
                }
            }
        }
    }
}

/**
 * Flat value track used by the floating panels: tap or drag anywhere to set the value, with ticks
 * every [tickStep] units marking the scale.
 */
@Composable
fun ReaderControlTrack(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    tickStep: Float = step,
    onInteraction: () -> Unit = {}
) {
    val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(step)

    fun valueAt(positionX: Float, widthPx: Float, insetPx: Float): Float {
        val activeWidthPx = (widthPx - insetPx * 2).coerceAtLeast(1f)
        val fraction = ((positionX - insetPx) / activeWidthPx).coerceIn(0f, 1f)
        val raw = valueRange.start + fraction * span
        val snapped = (raw / step).roundToInt() * step
        return snapped.coerceIn(valueRange.start, valueRange.endInclusive)
    }

    BoxWithConstraints(
        modifier = modifier
            .height(TRACK_HEIGHT)
            .clip(RoundedCornerShape(TRACK_HEIGHT / 2))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(valueRange, step) {
                detectTapGestures { offset ->
                    onInteraction()
                    onValueChange(valueAt(offset.x, size.width.toFloat(), TRACK_INSET.toPx()))
                }
            }
            .pointerInput(valueRange, step) {
                detectHorizontalDragGestures { change, _ ->
                    onInteraction()
                    change.consume()
                    onValueChange(
                        valueAt(change.position.x, size.width.toFloat(), TRACK_INSET.toPx())
                    )
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
        val filledWidth = when {
            value <= valueRange.start -> 0.dp
            value >= valueRange.endInclusive -> maxWidth
            else -> (TRACK_INSET + (maxWidth - TRACK_INSET * 2) * fraction).coerceAtLeast(0.dp)
        }

        if (filledWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .width(filledWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        val tickCount = (span / tickStep).roundToInt()
        for (tick in 0..tickCount) {
            val tickValue = valueRange.start + tick * tickStep
            val tickOffset = (
                TRACK_INSET + (maxWidth - TRACK_INSET * 2) * (tick.toFloat() / tickCount) - 1.5.dp
                ).coerceAtLeast(0.dp)
            val isFilled = tickValue <= value && value > valueRange.start
            Box(
                modifier = Modifier
                    .offset(x = tickOffset)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isFilled -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            tickValue <= value -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                    .align(Alignment.CenterStart)
            )
        }
    }
}
