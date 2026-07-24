/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.presentation.reader
import androidx.compose.ui.res.painterResource

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.translation.TranslationProviderMode
import com.byteflipper.everbook.presentation.core.components.common.StyledText
import com.byteflipper.everbook.presentation.core.util.noRippleClickable
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.ui.reader.ReaderTranslationState
import kotlinx.coroutines.delay

@Composable
fun LazyItemScope.ReaderInlineTranslatedParagraph(
    originalText: String,
    translation: ReaderTranslationState,
    closing: Boolean,
    paragraphTextStyle: TextStyle,
    fontColor: Color,
    sidePadding: Dp,
    horizontalAlignment: Alignment.Horizontal,
    openExternalTranslator: () -> Unit,
    closeTranslation: () -> Unit,
    toggleReaderMenu: () -> Unit,
    toggleTranslationOriginal: () -> Unit
) {
    val context = LocalContext.current
    val displayedText = when {
        translation.showOriginal -> originalText
        translation.isDownloadingModel &&
                translation.providerMode == TranslationProviderMode.IN_APP ->
            stringResource(id = R.string.translation_downloading_model)
        translation.isTranslating -> stringResource(id = R.string.translation_translating)
        translation.errorMessage != null -> translation.errorMessage
        else -> translation.translatedText ?: originalText
    }
    val canToggleOriginal = translation.translatedText != null || translation.errorMessage != null
    var selectionPulseActive by remember(translation.readerTextIndex, translation.text) {
        mutableStateOf(true)
    }

    LaunchedEffect(translation.readerTextIndex, translation.text) {
        selectionPulseActive = true
        delay(180)
        selectionPulseActive = false
    }

    // Derive every colour from the reader's own font colour so the block and its text always
    // contrast with whatever background the user configured. The block fill is a translucent tint
    // of the font colour painted over the real reader background, so it stays visible on any preset.
    val accentColor = fontColor
    val secondaryColor = fontColor.copy(alpha = 0.68f)
    val pulseBackground = fontColor.copy(alpha = 0.20f)
    val pulseRestBackground = fontColor.copy(alpha = 0.15f)
    val restingBackground = fontColor.copy(alpha = 0.11f)
    val targetBackground = when {
        closing -> Color.Transparent
        selectionPulseActive -> pulseBackground
        else -> restingBackground
    }
    val backgroundColor by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = if (selectionPulseActive) {
            keyframes {
                durationMillis = 180
                pulseBackground at 0
                pulseRestBackground at 180
            }
        } else {
            tween(220)
        },
        label = "InlineTranslationBackground"
    )
    val contentStartPadding by animateDpAsState(
        targetValue = if (closing) 0.dp else 10.dp,
        animationSpec = tween(180),
        label = "InlineTranslationContentStartPadding"
    )
    val contentVerticalPadding by animateDpAsState(
        targetValue = if (closing) 0.dp else 8.dp,
        animationSpec = tween(180),
        label = "InlineTranslationContentVerticalPadding"
    )

    Column(
        modifier = Modifier
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .fillMaxWidth()
            .padding(horizontal = sidePadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .animateContentSize(animationSpec = tween(220))
                .background(backgroundColor)
                .noRippleClickable(onClick = toggleReaderMenu)
        ) {
            InlineTranslationAccent(
                translating = translation.isTranslating,
                closing = closing,
                accentColor = accentColor
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(animationSpec = tween(220))
                    .padding(
                        start = contentStartPadding,
                        top = contentVerticalPadding,
                        end = 8.dp,
                        bottom = contentVerticalPadding
                    )
            ) {
                InlineTranslationText(
                    text = displayedText,
                    translating = translation.isTranslating,
                    closing = closing,
                    originalText = originalText,
                    skeletonColor = secondaryColor,
                    style = paragraphTextStyle.copy(
                        color = fontColor,
                        textIndent = TextIndent.None
                    )
                )

                AnimatedVisibility(
                    visible = !closing,
                    enter = fadeIn(animationSpec = tween(180)) +
                            scaleIn(animationSpec = tween(180), initialScale = 0.98f),
                    exit = fadeOut(animationSpec = tween(90)) +
                            scaleOut(animationSpec = tween(90), targetScale = 0.98f) +
                            shrinkVertically(animationSpec = tween(120))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))

                        InlineTranslationActions(
                            showGoogleAttribution = translation.providerMode ==
                                    TranslationProviderMode.GOOGLE_TRANSLATE,
                            showOriginal = translation.showOriginal,
                            canToggleOriginal = canToggleOriginal,
                            translating = translation.isTranslating,
                            canCopy = displayedText.isNotBlank() && !translation.isTranslating,
                            contentColor = secondaryColor,
                            toggleTranslationOriginal = toggleTranslationOriginal,
                            copyTranslation = {
                                copyInlineTranslationText(context = context, text = displayedText)
                            },
                            openExternalTranslator = openExternalTranslator,
                            closeTranslation = closeTranslation
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineTranslationAccent(
    translating: Boolean,
    closing: Boolean,
    accentColor: Color
) {
    val transition = rememberInfiniteTransition(label = "InlineTranslationAccent")
    val accentAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "InlineTranslationAccentAlpha"
    )
    val accentWidth by animateDpAsState(
        targetValue = if (closing) 0.dp else 4.dp,
        animationSpec = tween(180),
        label = "InlineTranslationAccentWidth"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(accentWidth)
            .alpha(if (translating) accentAlpha else 1f)
            .background(accentColor)
    )
}

@Composable
private fun InlineTranslationText(
    text: String,
    translating: Boolean,
    closing: Boolean,
    originalText: String,
    skeletonColor: Color,
    style: TextStyle
) {
    val targetText = if (closing) originalText else text

    AnimatedContent(
        targetState = translating && !closing,
        transitionSpec = {
            (fadeIn(animationSpec = tween(180)) +
                    scaleIn(animationSpec = tween(180), initialScale = 0.985f))
                .togetherWith(
                    fadeOut(animationSpec = tween(120)) +
                            scaleOut(animationSpec = tween(120), targetScale = 0.985f)
                )
                .using(SizeTransform(clip = false))
        },
        label = "InlineTranslationText"
    ) { isTranslating ->
        if (isTranslating) {
            InlineTranslationSkeleton(
                lineHeight = style.lineHeight,
                fontSize = style.fontSize,
                shimmerColor = skeletonColor
            )
        } else {
            AnimatedContent(
                targetState = targetText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) +
                            scaleIn(animationSpec = tween(180), initialScale = 0.985f))
                        .togetherWith(
                            fadeOut(animationSpec = tween(120)) +
                                    scaleOut(animationSpec = tween(120), targetScale = 0.985f)
                        )
                        .using(SizeTransform(clip = false))
                },
                label = "InlineTranslationDisplayedText"
            ) { displayedText ->
                StyledText(
                    text = displayedText,
                    modifier = Modifier.fillMaxWidth(),
                    style = style
                )
            }
        }
    }
}

@Composable
private fun InlineTranslationSkeleton(
    lineHeight: TextUnit,
    fontSize: TextUnit,
    shimmerColor: Color
) {
    val transition = rememberInfiniteTransition(label = "InlineTranslationSkeleton")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InlineTranslationSkeletonOffset"
    )
    val baseColor = shimmerColor.copy(alpha = 0.18f)
    val highlightColor = shimmerColor.copy(alpha = 0.42f)
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(shimmerOffset - 900f, 0f),
        end = Offset(shimmerOffset, 0f)
    )
    val lineHeightDp = if (lineHeight.isSp) {
        with(androidx.compose.ui.platform.LocalDensity.current) {
            lineHeight.toDp()
        }
    } else {
        18.dp
    }
    val fontSizeDp = if (fontSize.isSp) {
        with(androidx.compose.ui.platform.LocalDensity.current) {
            fontSize.toDp()
        }
    } else {
        14.dp
    }
    val skeletonLineHeight = (fontSizeDp * 0.72f).coerceAtLeast(8.dp)
    val skeletonSpacing = (lineHeightDp - skeletonLineHeight).coerceAtLeast(4.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(skeletonSpacing)
    ) {
        InlineTranslationSkeletonLine(
            widthFraction = 1f,
            height = skeletonLineHeight,
            brush = brush
        )
        InlineTranslationSkeletonLine(
            widthFraction = 0.92f,
            height = skeletonLineHeight,
            brush = brush
        )
        InlineTranslationSkeletonLine(
            widthFraction = 0.64f,
            height = skeletonLineHeight,
            brush = brush
        )
    }
}

@Composable
private fun InlineTranslationSkeletonLine(
    widthFraction: Float,
    height: Dp,
    brush: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .background(brush)
    )
}

@Composable
private fun InlineTranslationActions(
    showGoogleAttribution: Boolean,
    showOriginal: Boolean,
    canToggleOriginal: Boolean,
    translating: Boolean,
    canCopy: Boolean,
    contentColor: Color,
    toggleTranslationOriginal: () -> Unit,
    copyTranslation: () -> Unit,
    openExternalTranslator: () -> Unit,
    closeTranslation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showGoogleAttribution) {
                StyledText(
                    text = stringResource(id = R.string.translation_powered_by_google),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = contentColor,
                        textAlign = TextAlign.Start
                    )
                )

                StyledText(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = contentColor
                    )
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .noRippleClickable(
                        enabled = canToggleOriginal,
                        onClick = toggleTranslationOriginal
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = if (showOriginal) painterResource(R.drawable.ic_translate_rounded_24px) else painterResource(R.drawable.ic_visibility_rounded_24px),
                    contentDescription = stringResource(
                        id = if (showOriginal) {
                            R.string.translation_show_translation
                        } else {
                            R.string.translation_show_original
                        }
                    ),
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                StyledText(
                    text = stringResource(
                        id = if (showOriginal) {
                            R.string.translation_show_translation
                        } else {
                            R.string.translation_show_original
                        }
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = contentColor
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = !translating,
            enter = fadeIn(animationSpec = tween(180, delayMillis = 120)) +
                    scaleIn(animationSpec = tween(180, delayMillis = 120), initialScale = 0.98f),
            exit = fadeOut(animationSpec = tween(90)) +
                    scaleOut(animationSpec = tween(90), targetScale = 0.98f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    enabled = canCopy,
                    onClick = copyTranslation,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_content_copy_rounded_24px),
                        contentDescription = stringResource(id = R.string.copy),
                        modifier = Modifier.size(18.dp),
                        tint = contentColor
                    )
                }

                InlineTranslationTextButton(
                    text = stringResource(id = R.string.translation_open_external),
                    contentColor = contentColor,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new_rounded_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = true,
                    onClick = openExternalTranslator
                )

                Spacer(modifier = Modifier.weight(1f))

                InlineTranslationTextButton(
                    text = stringResource(id = R.string.close),
                    contentColor = contentColor,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_rounded_24px),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = true,
                    onClick = closeTranslation
                )
            }
        }
    }
}

@Composable
private fun InlineTranslationTextButton(
    text: String,
    contentColor: Color,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        StyledText(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun copyInlineTranslationText(context: Context, text: String) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", text))
    context.getString(R.string.copied).showToast(context = context, longToast = false)
}
