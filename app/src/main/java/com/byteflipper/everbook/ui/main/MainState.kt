@file:Suppress("UNCHECKED_CAST")

package com.byteflipper.everbook.ui.main

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.Preferences
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.browse.BrowseFilesStructure
import com.byteflipper.everbook.domain.browse.BrowseLayout
import com.byteflipper.everbook.domain.browse.BrowseSortOrder
import com.byteflipper.everbook.domain.browse.toBrowseFilesStructure
import com.byteflipper.everbook.domain.browse.toBrowseLayout
import com.byteflipper.everbook.domain.browse.toBrowseSortOrder
import com.byteflipper.everbook.domain.reader.ReaderColorEffects
import com.byteflipper.everbook.domain.reader.ReaderHorizontalGesture
import com.byteflipper.everbook.domain.reader.ReaderScreenOrientation
import com.byteflipper.everbook.domain.reader.ReaderTextAlignment
import com.byteflipper.everbook.domain.reader.toColorEffects
import com.byteflipper.everbook.domain.reader.toHorizontalGesture
import com.byteflipper.everbook.domain.reader.toReaderScreenOrientation
import com.byteflipper.everbook.domain.reader.toTextAlignment
import com.byteflipper.everbook.domain.util.HorizontalAlignment
import com.byteflipper.everbook.domain.util.toHorizontalAlignment
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.presentation.core.constants.provideLanguages
import com.byteflipper.everbook.ui.theme.DarkTheme
import com.byteflipper.everbook.ui.theme.PureDark
import com.byteflipper.everbook.ui.theme.Theme
import com.byteflipper.everbook.ui.theme.ThemeContrast
import com.byteflipper.everbook.ui.theme.toDarkTheme
import com.byteflipper.everbook.ui.theme.toPureDark
import com.byteflipper.everbook.ui.theme.toTheme
import com.byteflipper.everbook.ui.theme.toThemeContrast
import java.util.Locale

/**
 * Main State.
 * All app's settings/preferences/permanent-variables are here.
 * Wrapped in SavedStateHandle, so it won't reset.
 */
@Immutable
@Keep
@Parcelize
data class MainState(
    // General Settings
    val language: String = provideDefaultValue {
        val locale = Locale.getDefault().language.take(2)
        Constants.provideLanguages().any { locale == it.first }.run {
            if (this) locale
            else "en"// Default language.
        }
    },
    val theme: Theme = provideDefaultValue { Theme.entries().first() },
    val darkTheme: DarkTheme = provideDefaultValue { DarkTheme.FOLLOW_SYSTEM },
    val pureDark: PureDark = provideDefaultValue { PureDark.OFF },
    val absoluteDark: Boolean = provideDefaultValue { false },
    val themeContrast: ThemeContrast = provideDefaultValue { ThemeContrast.STANDARD },
    val showStartScreen: Boolean = provideDefaultValue { true },
    val checkForUpdates: Boolean = provideDefaultValue { false },
    val doublePressExit: Boolean = provideDefaultValue { false },

    // Reader Settings
    val fontFamily: String = provideDefaultValue { Constants.provideFonts(withRandom = false)[0].id },
    val isItalic: Boolean = provideDefaultValue { false },
    val fontSize: Int = provideDefaultValue { 16 },
    val lineHeight: Int = provideDefaultValue { 4 },
    val paragraphHeight: Int = provideDefaultValue { 8 },
    val paragraphIndentation: Int = provideDefaultValue { 0 },
    val sidePadding: Int = provideDefaultValue { 6 },
    val verticalPadding: Int = provideDefaultValue { 0 },
    val doubleClickTranslation: Boolean = provideDefaultValue { false },
    val fastColorPresetChange: Boolean = provideDefaultValue { true },
    val textAlignment: ReaderTextAlignment = provideDefaultValue { ReaderTextAlignment.JUSTIFY },
    val letterSpacing: Int = provideDefaultValue { 0 },
    val cutoutPadding: Boolean = provideDefaultValue { false },
    val fullscreen: Boolean = provideDefaultValue { true },
    val keepScreenOn: Boolean = provideDefaultValue { true },
    val hideBarsOnFastScroll: Boolean = provideDefaultValue { false },
    val perceptionExpander: Boolean = provideDefaultValue { false },
    val perceptionExpanderPadding: Int = provideDefaultValue { 5 },
    val perceptionExpanderThickness: Int = provideDefaultValue { 4 },
    val screenOrientation: ReaderScreenOrientation = provideDefaultValue {
        ReaderScreenOrientation.DEFAULT
    },
    val customScreenBrightness: Boolean = provideDefaultValue { false },
    val screenBrightness: Float = provideDefaultValue { 0.5f },
    val horizontalGesture: ReaderHorizontalGesture = provideDefaultValue {
        ReaderHorizontalGesture.OFF
    },
    val horizontalGestureScroll: Float = provideDefaultValue { 0.7f },
    val horizontalGestureSensitivity: Float = provideDefaultValue { 0.6f },
    val bottomBarPadding: Int = provideDefaultValue { 0 },
    val highlightedReading: Boolean = provideDefaultValue { false },
    val highlightedReadingThickness: Int = provideDefaultValue { 2 },
    val chapterTitleAlignment: ReaderTextAlignment = provideDefaultValue { ReaderTextAlignment.JUSTIFY },
    val images: Boolean = provideDefaultValue { true },
    val imagesCornersRoundness: Int = provideDefaultValue { 8 },
    val imagesAlignment: HorizontalAlignment = provideDefaultValue { HorizontalAlignment.START },
    val imagesWidth: Float = provideDefaultValue { 0.8f },
    val imagesColorEffects: ReaderColorEffects = provideDefaultValue { ReaderColorEffects.OFF },
    val progressBar: Boolean = provideDefaultValue { false },
    val progressBarPadding: Int = provideDefaultValue { 4 },

    // Browse Settings
    val browseFilesStructure: BrowseFilesStructure = provideDefaultValue {
        BrowseFilesStructure.DIRECTORIES
    },
    val browseLayout: BrowseLayout = provideDefaultValue { BrowseLayout.LIST },
    val browseAutoGridSize: Boolean = provideDefaultValue { true },
    val browseGridSize: Int = provideDefaultValue { 0 },
    val browsePinFavoriteDirectories: Boolean = provideDefaultValue { true },
    val browseSortOrder: BrowseSortOrder = provideDefaultValue { BrowseSortOrder.LAST_MODIFIED },
    val browseSortOrderDescending: Boolean = provideDefaultValue { true },
    val browseIncludedFilterItems: List<String> = provideDefaultValue { emptyList() },
) : Parcelable {
    companion object {
        private fun <D> provideDefaultValue(calculation: () -> D): D {
            return calculation()
        }

        /**
         * Initializes [MainState] by given [Map].
         * If no value provided in [data], assigns default value.
         */
        fun initialize(data: Map<String, Any>): MainState {
            val defaultState = MainState()
            fun <V, T> provideValue(
                key: Preferences.Key<T>,
                convert: T.() -> V = { this as V },
                default: MainState.() -> V
            ): V {
                return (data[key.name] as? T)?.convert() ?: defaultState.default()
            }

            return DataStoreConstants.run {
                MainState(
                    language = provideValue(
                        LANGUAGE
                    ) { language },

                    theme = provideValue(
                        THEME, convert = { toTheme() }
                    ) { theme },

                    darkTheme = provideValue(
                        DARK_THEME, convert = { toDarkTheme() }
                    ) { darkTheme },

                    pureDark = provideValue(
                        PURE_DARK, convert = { toPureDark() }
                    ) { pureDark },

                    absoluteDark = provideValue(
                        ABSOLUTE_DARK
                    ) { absoluteDark },

                    themeContrast = provideValue(
                        THEME_CONTRAST, convert = { toThemeContrast() }
                    ) { themeContrast },

                    showStartScreen = provideValue(
                        SHOW_START_SCREEN
                    ) { showStartScreen },

                    fontFamily = provideValue(
                        FONT
                    ) { fontFamily },

                    isItalic = provideValue(
                        IS_ITALIC
                    ) { isItalic },

                    fontSize = provideValue(
                        FONT_SIZE
                    ) { fontSize },

                    lineHeight = provideValue(
                        LINE_HEIGHT
                    ) { lineHeight },

                    paragraphHeight = provideValue(
                        PARAGRAPH_HEIGHT
                    ) { paragraphHeight },

                    paragraphIndentation = provideValue(
                        PARAGRAPH_INDENTATION
                    ) { paragraphIndentation },

                    checkForUpdates = provideValue(
                        CHECK_FOR_UPDATES
                    ) { checkForUpdates },

                    sidePadding = provideValue(
                        SIDE_PADDING
                    ) { sidePadding },

                    doubleClickTranslation = provideValue(
                        DOUBLE_CLICK_TRANSLATION
                    ) { doubleClickTranslation },

                    fastColorPresetChange = provideValue(
                        FAST_COLOR_PRESET_CHANGE
                    ) { fastColorPresetChange },

                    browseFilesStructure = provideValue(
                        BROWSE_FILES_STRUCTURE, convert = { toBrowseFilesStructure() }
                    ) { browseFilesStructure },

                    browseLayout = provideValue(
                        BROWSE_LAYOUT, convert = { toBrowseLayout() }
                    ) { browseLayout },

                    browseAutoGridSize = provideValue(
                        BROWSE_AUTO_GRID_SIZE
                    ) { browseAutoGridSize },

                    browseGridSize = provideValue(
                        BROWSE_GRID_SIZE
                    ) { browseGridSize },

                    browsePinFavoriteDirectories = provideValue(
                        BROWSE_PIN_FAVORITE_DIRECTORIES
                    ) { browsePinFavoriteDirectories },

                    browseSortOrder = provideValue(
                        BROWSE_SORT_ORDER, convert = { toBrowseSortOrder() }
                    ) { browseSortOrder },

                    browseSortOrderDescending = provideValue(
                        BROWSE_SORT_ORDER_DESCENDING
                    ) { browseSortOrderDescending },

                    browseIncludedFilterItems = provideValue(
                        BROWSE_INCLUDED_FILTER_ITEMS, convert = { toList() }
                    ) { browseIncludedFilterItems },

                    textAlignment = provideValue(
                        TEXT_ALIGNMENT, convert = { toTextAlignment() }
                    ) { textAlignment },

                    doublePressExit = provideValue(
                        DOUBLE_PRESS_EXIT
                    ) { doublePressExit },

                    letterSpacing = provideValue(
                        LETTER_SPACING
                    ) { letterSpacing },

                    cutoutPadding = provideValue(
                        CUTOUT_PADDING
                    ) { cutoutPadding },

                    fullscreen = provideValue(
                        FULLSCREEN
                    ) { fullscreen },

                    keepScreenOn = provideValue(
                        KEEP_SCREEN_ON
                    ) { keepScreenOn },

                    verticalPadding = provideValue(
                        VERTICAL_PADDING
                    ) { verticalPadding },

                    hideBarsOnFastScroll = provideValue(
                        HIDE_BARS_ON_FAST_SCROLL
                    ) { hideBarsOnFastScroll },

                    perceptionExpander = provideValue(
                        PERCEPTION_EXPANDER
                    ) { perceptionExpander },

                    perceptionExpanderPadding = provideValue(
                        PERCEPTION_EXPANDER_PADDING
                    ) { perceptionExpanderPadding },

                    perceptionExpanderThickness = provideValue(
                        PERCEPTION_EXPANDER_THICKNESS
                    ) { perceptionExpanderThickness },

                    screenOrientation = provideValue(
                        SCREEN_ORIENTATION, convert = { toReaderScreenOrientation() }
                    ) { screenOrientation },

                    customScreenBrightness = provideValue(
                        CUSTOM_SCREEN_BRIGHTNESS
                    ) { customScreenBrightness },

                    screenBrightness = provideValue(
                        SCREEN_BRIGHTNESS, convert = { this.toFloat() }
                    ) { screenBrightness },

                    horizontalGesture = provideValue(
                        HORIZONTAL_GESTURE, convert = { toHorizontalGesture() }
                    ) { horizontalGesture },

                    horizontalGestureScroll = provideValue(
                        HORIZONTAL_GESTURE_SCROLL, convert = { toFloat() }
                    ) { horizontalGestureScroll },

                    horizontalGestureSensitivity = provideValue(
                        HORIZONTAL_GESTURE_SENSITIVITY, convert = { toFloat() }
                    ) { horizontalGestureSensitivity },

                    bottomBarPadding = provideValue(
                        BOTTOM_BAR_PADDING
                    ) { bottomBarPadding },

                    highlightedReading = provideValue(
                        HIGHLIGHTED_READING
                    ) { highlightedReading },

                    highlightedReadingThickness = provideValue(
                        HIGHLIGHTED_READING_THICKNESS
                    ) { highlightedReadingThickness },

                    chapterTitleAlignment = provideValue(
                        CHAPTER_TITLE_ALIGNMENT, convert = { toTextAlignment() }
                    ) { chapterTitleAlignment },

                    images = provideValue(
                        IMAGES
                    ) { images },

                    imagesCornersRoundness = provideValue(
                        IMAGES_CORNERS_ROUNDNESS
                    ) { imagesCornersRoundness },

                    imagesAlignment = provideValue(
                        IMAGES_ALIGNMENT, convert = { toHorizontalAlignment() }
                    ) { imagesAlignment },

                    imagesWidth = provideValue(
                        IMAGES_WIDTH, convert = { toFloat() }
                    ) { imagesWidth },

                    imagesColorEffects = provideValue(
                        IMAGES_COLOR_EFFECTS, convert = { toColorEffects() }
                    ) { imagesColorEffects },

                    progressBar = provideValue(
                        PROGRESS_BAR
                    ) { progressBar },

                    progressBarPadding = provideValue(
                        PROGRESS_BAR_PADDING
                    ) { progressBarPadding },
                )
            }
        }
    }
}