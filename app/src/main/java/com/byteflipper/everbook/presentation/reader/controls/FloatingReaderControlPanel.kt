/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader.controls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.runtime.derivedStateOf
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
private const val SETTINGS_HEIGHT_FRACTION = 0.55f
private val SETTINGS_MAX_HEIGHT = 480.dp
private val COLLAPSED_HEIGHT = 40.dp
private val COLLAPSED_CORNER = 20.dp
private val EXPANDED_CORNER = 28.dp
private val TRACK_HEIGHT = 28.dp
private val TRACK_INSET = 14.dp

/** Sizes a floating panel can take, ordered from the smallest to the largest. */
private enum class ReaderPanelPhase { Chip, Controls, Settings }

/**
 * Spring shared by every animated dimension of the panel, so height, width and corners always
 * travel together. Growing into the settings covers the longest distance and gets the softest
 * spring; shrinking back is quicker and free of overshoot, because a large surface that overshoots
 * looks sloppy; the chip keeps a hint of bounce.
 */
private fun <T> Transition.Segment<ReaderPanelPhase>.spatialSpec(): FiniteAnimationSpec<T> = when {
    targetState == ReaderPanelPhase.Settings -> spring(dampingRatio = 0.88f, stiffness = 500f)
    initialState == ReaderPanelPhase.Settings -> spring(dampingRatio = 1f, stiffness = 900f)
    else -> spring(dampingRatio = 0.78f, stiffness = 900f)
}

/**
 * Opacity is an effect rather than a size, so it is timed instead of sprung. Fading down to the
 * chip waits for the shrink to finish, otherwise the panel turns translucent while it is still wide.
 */
private fun Transition.Segment<ReaderPanelPhase>.alphaSpec(): FiniteAnimationSpec<Float> =
    if (targetState == ReaderPanelPhase.Chip) {
        tween(durationMillis = 250, delayMillis = 300)
    } else {
        tween(durationMillis = 150)
    }

/**
 * Cross-fade between the contents of two phases, nudged along the axis the panel grows in. The
 * card is sized by the very same transition, so the content must not animate its size on its own.
 */
private fun AnimatedContentTransitionScope<ReaderPanelPhase>.contentSpec(): ContentTransform {
    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
    return ContentTransform(
        targetContentEnter = fadeIn(animationSpec = tween(200, delayMillis = 80)) +
            slideInVertically(animationSpec = tween(300)) { height -> direction * height / 16 },
        initialContentExit = fadeOut(animationSpec = tween(140)) +
            slideOutVertically(animationSpec = tween(300)) { height -> -direction * height / 16 },
        sizeTransform = null
    )
}

/**
 * State of a floating reader panel: whether it is shrunk to a chip, whether it shows its settings
 * instead of the session controls, and the auto-collapse countdown that ties them together.
 */
@Stable
class ReaderPanelCollapseState internal constructor(
    private val collapsed: MutableState<Boolean>,
    private val settingsShown: MutableState<Boolean>,
    private val lastInteractionAt: MutableState<Long>
) {
    val isCollapsed: Boolean get() = collapsed.value

    val isSettingsShown: Boolean get() = settingsShown.value

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

    /** Switches the panel between its session controls and its settings. */
    fun toggleSettings() {
        settingsShown.value = !settingsShown.value
        expand()
    }

    internal fun reset() {
        settingsShown.value = false
        expand()
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
    val settingsShown = rememberSaveable(resetKey) { mutableStateOf(false) }
    val lastInteractionAt = remember(resetKey) { mutableLongStateOf(System.currentTimeMillis()) }
    val state = remember(collapsed, settingsShown, lastInteractionAt) {
        ReaderPanelCollapseState(collapsed, settingsShown, lastInteractionAt)
    }

    // The settings hold the panel open: shrinking into a chip would take away the very control the
    // user is adjusting, and adjusting a setting is not necessarily a touch on the panel.
    LaunchedEffect(active, state.isCollapsed, state.isSettingsShown, state.interactionMark) {
        if (active && !state.isCollapsed && !state.isSettingsShown) {
            delay(collapseDelayMillis)
            state.collapse()
        }
    }

    LaunchedEffect(active) {
        if (!active) state.reset()
    }

    return state
}

/**
 * Card floating above the reader that holds the controls of a running session (auto-scroll or
 * read aloud). It shows [expandedContent] until the auto-collapse timer fires, then shrinks into a
 * chip showing [collapsedContent]; tapping the chip expands it again.
 *
 * When [settingsContent] is supplied, the panel gains a third size: `collapseState.toggleSettings()`
 * grows the card and cross-fades the controls into those settings. Unlike [expandedContent], the
 * settings get the whole card and lay out their own padding.
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
    settingsContent: (@Composable ColumnScope.() -> Unit)? = null,
    collapsedContent: @Composable RowScope.() -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        val hasSettings = settingsContent != null
        val phase by remember(collapseState, hasSettings) {
            derivedStateOf {
                when {
                    collapseState.isCollapsed -> ReaderPanelPhase.Chip
                    hasSettings && collapseState.isSettingsShown -> ReaderPanelPhase.Settings
                    else -> ReaderPanelPhase.Controls
                }
            }
        }
        // The card grows from its anchored corner, so its content is anchored the same way and is
        // simply unveiled instead of being re-laid out on every frame of the animation.
        val panelAlignment = when (alignment) {
            READER_PANEL_BOTTOM_LEFT -> Alignment.BottomStart
            READER_PANEL_BOTTOM_CENTER -> Alignment.BottomCenter
            else -> Alignment.BottomEnd
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentAlignment = panelAlignment
        ) {
            // The settings take a share of what the reader leaves free instead of a fixed height,
            // so the card never grows past the space between the insets.
            val settingsHeight = minOf(
                maxHeight * SETTINGS_HEIGHT_FRACTION,
                SETTINGS_MAX_HEIGHT
            ).coerceAtLeast(expandedHeight).coerceAtMost(maxHeight)
            val panelWidth = maxWidth

            val transition = updateTransition(targetState = phase, label = "ReaderPanelPhase")
            val panelHeight by transition.animateDp(
                transitionSpec = { spatialSpec() },
                label = "PanelHeight"
            ) { target ->
                when (target) {
                    ReaderPanelPhase.Chip -> COLLAPSED_HEIGHT
                    ReaderPanelPhase.Controls -> expandedHeight
                    ReaderPanelPhase.Settings -> settingsHeight
                }
            }
            val panelWidthFraction by transition.animateFloat(
                transitionSpec = { spatialSpec() },
                label = "PanelWidth"
            ) { target ->
                if (target == ReaderPanelPhase.Chip) collapsedWidthFraction else 1f
            }
            val panelCorner by transition.animateDp(
                transitionSpec = { spatialSpec() },
                label = "PanelCorner"
            ) { target ->
                if (target == ReaderPanelPhase.Chip) COLLAPSED_CORNER else EXPANDED_CORNER
            }
            val panelAlpha by transition.animateFloat(
                transitionSpec = { alphaSpec() },
                label = "PanelAlpha"
            ) { target ->
                if (target == ReaderPanelPhase.Chip) collapsedAlpha else 1f
            }

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
                transition.AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = { contentSpec() },
                    contentAlignment = panelAlignment
                ) { target ->
                    when (target) {
                        ReaderPanelPhase.Chip -> Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(COLLAPSED_HEIGHT)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            content = collapsedContent
                        )

                        ReaderPanelPhase.Controls -> Column(
                            modifier = Modifier
                                .requiredWidth(panelWidth)
                                .requiredHeight(expandedHeight)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            content = expandedContent
                        )

                        ReaderPanelPhase.Settings -> if (settingsContent != null) {
                            Column(
                                modifier = Modifier
                                    .requiredWidth(panelWidth)
                                    .requiredHeight(settingsHeight)
                                    .padding(vertical = 12.dp),
                                content = settingsContent
                            )
                        }
                    }
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
