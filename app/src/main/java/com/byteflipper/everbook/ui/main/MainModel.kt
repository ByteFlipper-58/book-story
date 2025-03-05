/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.main

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import com.byteflipper.everbook.domain.browse.toBrowseLayout
import com.byteflipper.everbook.domain.browse.toBrowseSortOrder
import com.byteflipper.everbook.domain.reader.toColorEffects
import com.byteflipper.everbook.domain.reader.toFontThickness
import com.byteflipper.everbook.domain.reader.toHorizontalGesture
import com.byteflipper.everbook.domain.reader.toProgressCount
import com.byteflipper.everbook.domain.reader.toReaderScreenOrientation
import com.byteflipper.everbook.domain.reader.toTextAlignment
import com.byteflipper.everbook.domain.use_case.data_store.ChangeLanguage
import com.byteflipper.everbook.domain.use_case.data_store.GetAllSettings
import com.byteflipper.everbook.domain.use_case.data_store.SetDatastore
import com.byteflipper.everbook.domain.use_case.remote.CheckForUpdates
import com.byteflipper.everbook.domain.util.toHorizontalAlignment
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import com.byteflipper.everbook.presentation.core.constants.provideFonts
import com.byteflipper.everbook.presentation.core.constants.provideMainState
import com.byteflipper.everbook.ui.theme.toDarkTheme
import com.byteflipper.everbook.ui.theme.toPureDark
import com.byteflipper.everbook.ui.theme.toTheme
import com.byteflipper.everbook.ui.theme.toThemeContrast
import javax.inject.Inject


@HiltViewModel
class MainModel @Inject constructor(
    private val stateHandle: SavedStateHandle,

    private val setDatastore: SetDatastore,
    private val changeLanguage: ChangeLanguage,
    private val checkForUpdates: CheckForUpdates,
    private val getAllSettings: GetAllSettings
) : ViewModel() {

    private val mutex = Mutex()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val mainModelReady = MutableStateFlow(false)

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(
        stateHandle[Constants.provideMainState()] ?: MainState()
    )
    val state = _state.asStateFlow()

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.OnChangeLanguage -> handleLanguageUpdate(event)

            is MainEvent.OnChangeDarkTheme -> handleDatastoreUpdate(
                key = DataStoreConstants.DARK_THEME,
                value = event.value,
                updateState = {
                    it.copy(darkTheme = toDarkTheme())
                }
            )

            is MainEvent.OnChangePureDark -> handleDatastoreUpdate(
                key = DataStoreConstants.PURE_DARK,
                value = event.value,
                updateState = {
                    it.copy(pureDark = toPureDark())
                }
            )

            is MainEvent.OnChangeThemeContrast -> handleDatastoreUpdate(
                key = DataStoreConstants.THEME_CONTRAST,
                value = event.value,
                updateState = {
                    it.copy(themeContrast = toThemeContrast())
                }
            )

            is MainEvent.OnChangeTheme -> handleDatastoreUpdate(
                key = DataStoreConstants.THEME,
                value = event.value,
                updateState = {
                    it.copy(theme = toTheme())
                }
            )

            is MainEvent.OnChangeFontFamily -> handleDatastoreUpdate(
                key = DataStoreConstants.FONT,
                value = event.value,
                updateState = {
                    it.copy(
                        fontFamily = Constants.provideFonts().run {
                            find { font ->
                                font.id == event.value
                            }?.id ?: get(0).id
                        }
                    )
                }
            )

            is MainEvent.OnChangeFontStyle -> handleDatastoreUpdate(
                key = DataStoreConstants.IS_ITALIC,
                value = event.value,
                updateState = {
                    it.copy(isItalic = this)
                }
            )

            is MainEvent.OnChangeFontSize -> handleDatastoreUpdate(
                key = DataStoreConstants.FONT_SIZE,
                value = event.value,
                updateState = {
                    it.copy(fontSize = this)
                }
            )

            is MainEvent.OnChangeLineHeight -> handleDatastoreUpdate(
                key = DataStoreConstants.LINE_HEIGHT,
                value = event.value,
                updateState = {
                    it.copy(lineHeight = this)
                }
            )

            is MainEvent.OnChangeParagraphHeight -> handleDatastoreUpdate(
                key = DataStoreConstants.PARAGRAPH_HEIGHT,
                value = event.value,
                updateState = {
                    it.copy(paragraphHeight = this)
                }
            )

            is MainEvent.OnChangeParagraphIndentation -> handleDatastoreUpdate(
                key = DataStoreConstants.PARAGRAPH_INDENTATION,
                value = event.value,
                updateState = {
                    it.copy(paragraphIndentation = this)
                }
            )

            is MainEvent.OnChangeShowStartScreen -> handleDatastoreUpdate(
                key = DataStoreConstants.SHOW_START_SCREEN,
                value = event.value,
                updateState = {
                    it.copy(showStartScreen = this)
                }
            )

            is MainEvent.OnChangeCheckForUpdates -> handleDatastoreUpdate(
                key = DataStoreConstants.CHECK_FOR_UPDATES,
                value = event.value,
                updateState = {
                    it.copy(checkForUpdates = this)
                }
            )

            is MainEvent.OnChangeSidePadding -> handleDatastoreUpdate(
                key = DataStoreConstants.SIDE_PADDING,
                value = event.value,
                updateState = {
                    it.copy(sidePadding = this)
                }
            )

            is MainEvent.OnChangeDoubleClickTranslation -> handleDatastoreUpdate(
                key = DataStoreConstants.DOUBLE_CLICK_TRANSLATION,
                value = event.value,
                updateState = {
                    it.copy(doubleClickTranslation = this)
                }
            )

            is MainEvent.OnChangeFastColorPresetChange -> handleDatastoreUpdate(
                key = DataStoreConstants.FAST_COLOR_PRESET_CHANGE,
                value = event.value,
                updateState = {
                    it.copy(fastColorPresetChange = this)
                }
            )

            is MainEvent.OnChangeBrowseLayout -> handleDatastoreUpdate(
                key = DataStoreConstants.BROWSE_LAYOUT,
                value = event.value,
                updateState = {
                    it.copy(browseLayout = toBrowseLayout())
                }
            )

            is MainEvent.OnChangeBrowseAutoGridSize -> handleDatastoreUpdate(
                key = DataStoreConstants.BROWSE_AUTO_GRID_SIZE,
                value = event.value,
                updateState = {
                    it.copy(browseAutoGridSize = this)
                }
            )

            is MainEvent.OnChangeBrowseGridSize -> handleDatastoreUpdate(
                key = DataStoreConstants.BROWSE_GRID_SIZE,
                value = event.value,
                updateState = {
                    it.copy(browseGridSize = this)
                }
            )

            is MainEvent.OnChangeBrowseSortOrder -> handleDatastoreUpdate(
                key = DataStoreConstants.BROWSE_SORT_ORDER,
                value = event.value,
                updateState = {
                    it.copy(browseSortOrder = toBrowseSortOrder())
                }
            )

            is MainEvent.OnChangeBrowseSortOrderDescending -> handleDatastoreUpdate(
                key = DataStoreConstants.BROWSE_SORT_ORDER_DESCENDING,
                value = event.value,
                updateState = {
                    it.copy(browseSortOrderDescending = this)
                }
            )

            is MainEvent.OnChangeBrowseIncludedFilterItem -> handleBrowseIncludedFilterItemUpdate(
                event = event
            )

            is MainEvent.OnChangeTextAlignment -> handleDatastoreUpdate(
                key = DataStoreConstants.TEXT_ALIGNMENT,
                value = event.value,
                updateState = {
                    it.copy(textAlignment = toTextAlignment())
                }
            )

            is MainEvent.OnChangeDoublePressExit -> handleDatastoreUpdate(
                key = DataStoreConstants.DOUBLE_PRESS_EXIT,
                value = event.value,
                updateState = {
                    it.copy(doublePressExit = this)
                }
            )

            is MainEvent.OnChangeLetterSpacing -> handleDatastoreUpdate(
                key = DataStoreConstants.LETTER_SPACING,
                value = event.value,
                updateState = {
                    it.copy(letterSpacing = this)
                }
            )

            is MainEvent.OnChangeAbsoluteDark -> handleDatastoreUpdate(
                key = DataStoreConstants.ABSOLUTE_DARK,
                value = event.value,
                updateState = {
                    it.copy(absoluteDark = this)
                }
            )

            is MainEvent.OnChangeCutoutPadding -> handleDatastoreUpdate(
                key = DataStoreConstants.CUTOUT_PADDING,
                value = event.value,
                updateState = {
                    it.copy(cutoutPadding = this)
                }
            )

            is MainEvent.OnChangeFullscreen -> handleDatastoreUpdate(
                key = DataStoreConstants.FULLSCREEN,
                value = event.value,
                updateState = {
                    it.copy(fullscreen = this)
                }
            )

            is MainEvent.OnChangeKeepScreenOn -> handleDatastoreUpdate(
                key = DataStoreConstants.KEEP_SCREEN_ON,
                value = event.value,
                updateState = {
                    it.copy(keepScreenOn = this)
                }
            )

            is MainEvent.OnChangeVerticalPadding -> handleDatastoreUpdate(
                key = DataStoreConstants.VERTICAL_PADDING,
                value = event.value,
                updateState = {
                    it.copy(verticalPadding = this)
                }
            )

            is MainEvent.OnChangeHideBarsOnFastScroll -> handleDatastoreUpdate(
                key = DataStoreConstants.HIDE_BARS_ON_FAST_SCROLL,
                value = event.value,
                updateState = {
                    it.copy(hideBarsOnFastScroll = this)
                }
            )

            is MainEvent.OnChangePerceptionExpander -> handleDatastoreUpdate(
                key = DataStoreConstants.PERCEPTION_EXPANDER,
                value = event.value,
                updateState = {
                    it.copy(perceptionExpander = this)
                }
            )

            is MainEvent.OnChangePerceptionExpanderPadding -> handleDatastoreUpdate(
                key = DataStoreConstants.PERCEPTION_EXPANDER_PADDING,
                value = event.value,
                updateState = {
                    it.copy(perceptionExpanderPadding = this)
                }
            )

            is MainEvent.OnChangePerceptionExpanderThickness -> handleDatastoreUpdate(
                key = DataStoreConstants.PERCEPTION_EXPANDER_THICKNESS,
                value = event.value,
                updateState = {
                    it.copy(perceptionExpanderThickness = this)
                }
            )

            is MainEvent.OnChangeScreenOrientation -> handleDatastoreUpdate(
                key = DataStoreConstants.SCREEN_ORIENTATION,
                value = event.value,
                updateState = {
                    it.copy(screenOrientation = toReaderScreenOrientation())
                }
            )

            is MainEvent.OnChangeCustomScreenBrightness -> handleDatastoreUpdate(
                key = DataStoreConstants.CUSTOM_SCREEN_BRIGHTNESS,
                value = event.value,
                updateState = {
                    it.copy(customScreenBrightness = this)
                }
            )

            is MainEvent.OnChangeScreenBrightness -> handleDatastoreUpdate(
                key = DataStoreConstants.SCREEN_BRIGHTNESS,
                value = event.value.toDouble(),
                updateState = {
                    it.copy(screenBrightness = this.toFloat())
                }
            )

            is MainEvent.OnChangeHorizontalGesture -> handleDatastoreUpdate(
                key = DataStoreConstants.HORIZONTAL_GESTURE,
                value = event.value,
                updateState = {
                    it.copy(horizontalGesture = toHorizontalGesture())
                }
            )

            is MainEvent.OnChangeHorizontalGestureScroll -> handleDatastoreUpdate(
                key = DataStoreConstants.HORIZONTAL_GESTURE_SCROLL,
                value = event.value.toDouble(),
                updateState = {
                    it.copy(horizontalGestureScroll = this.toFloat())
                }
            )

            is MainEvent.OnChangeHorizontalGestureSensitivity -> handleDatastoreUpdate(
                key = DataStoreConstants.HORIZONTAL_GESTURE_SENSITIVITY,
                value = event.value.toDouble(),
                updateState = {
                    it.copy(horizontalGestureSensitivity = this.toFloat())
                }
            )

            is MainEvent.OnChangeBottomBarPadding -> handleDatastoreUpdate(
                key = DataStoreConstants.BOTTOM_BAR_PADDING,
                value = event.value,
                updateState = {
                    it.copy(bottomBarPadding = this)
                }
            )

            is MainEvent.OnChangeHighlightedReading -> handleDatastoreUpdate(
                key = DataStoreConstants.HIGHLIGHTED_READING,
                value = event.value,
                updateState = {
                    it.copy(highlightedReading = this)
                }
            )

            is MainEvent.OnChangeHighlightedReadingThickness -> handleDatastoreUpdate(
                key = DataStoreConstants.HIGHLIGHTED_READING_THICKNESS,
                value = event.value,
                updateState = {
                    it.copy(highlightedReadingThickness = this)
                }
            )

            is MainEvent.OnChangeChapterTitleAlignment -> handleDatastoreUpdate(
                key = DataStoreConstants.CHAPTER_TITLE_ALIGNMENT,
                value = event.value,
                updateState = {
                    it.copy(chapterTitleAlignment = toTextAlignment())
                }
            )

            is MainEvent.OnChangeImages -> handleDatastoreUpdate(
                key = DataStoreConstants.IMAGES,
                value = event.value,
                updateState = {
                    it.copy(images = this)
                }
            )

            is MainEvent.OnChangeImagesCornersRoundness -> handleDatastoreUpdate(
                key = DataStoreConstants.IMAGES_CORNERS_ROUNDNESS,
                value = event.value,
                updateState = {
                    it.copy(imagesCornersRoundness = this)
                }
            )

            is MainEvent.OnChangeImagesAlignment -> handleDatastoreUpdate(
                key = DataStoreConstants.IMAGES_ALIGNMENT,
                value = event.value,
                updateState = {
                    it.copy(imagesAlignment = this.toHorizontalAlignment())
                }
            )

            is MainEvent.OnChangeImagesWidth -> handleDatastoreUpdate(
                key = DataStoreConstants.IMAGES_WIDTH,
                value = event.value.toDouble(),
                updateState = {
                    it.copy(imagesWidth = this.toFloat())
                }
            )

            is MainEvent.OnChangeImagesColorEffects -> handleDatastoreUpdate(
                key = DataStoreConstants.IMAGES_COLOR_EFFECTS,
                value = event.value,
                updateState = {
                    it.copy(imagesColorEffects = this.toColorEffects())
                }
            )

            is MainEvent.OnChangeProgressBar -> handleDatastoreUpdate(
                key = DataStoreConstants.PROGRESS_BAR,
                value = event.value,
                updateState = {
                    it.copy(progressBar = this)
                }
            )

            is MainEvent.OnChangeProgressBarPadding -> handleDatastoreUpdate(
                key = DataStoreConstants.PROGRESS_BAR_PADDING,
                value = event.value,
                updateState = {
                    it.copy(progressBarPadding = this)
                }
            )

            is MainEvent.OnChangeProgressBarAlignment -> handleDatastoreUpdate(
                key = DataStoreConstants.PROGRESS_BAR_ALIGNMENT,
                value = event.value,
                updateState = {
                    it.copy(progressBarAlignment = this.toHorizontalAlignment())
                }
            )

            is MainEvent.OnChangeProgressBarFontSize -> handleDatastoreUpdate(
                key = DataStoreConstants.PROGRESS_BAR_FONT_SIZE,
                value = event.value,
                updateState = {
                    it.copy(progressBarFontSize = this)
                }
            )

            is MainEvent.OnChangeBrowsePinnedPaths -> handleBrowsePinnedPathsUpdate(
                event = event
            )

            is MainEvent.OnChangeFontThickness -> handleDatastoreUpdate(
                key = DataStoreConstants.FONT_THICKNESS,
                value = event.value,
                updateState = {
                    it.copy(fontThickness = this.toFontThickness())
                }
            )

            is MainEvent.OnChangeProgressCount -> handleDatastoreUpdate(
                key = DataStoreConstants.PROGRESS_COUNT,
                value = event.value,
                updateState = {
                    it.copy(progressCount = this.toProgressCount())
                }
            )
        }
    }

    fun init(settingsModelReady: StateFlow<Boolean>) {
        viewModelScope.launch(Dispatchers.Main) {
            val settings = getAllSettings.execute()

            // All additional execution
            changeLanguage.execute(settings.language)

            if (settings.checkForUpdates) {
                viewModelScope.launch(Dispatchers.IO) {
                    checkForUpdates.execute(
                        postNotification = true
                    )
                }
            }
            /* - - - - - - - - - - - */

            updateStateWithSavedHandle { settings }
            mainModelReady.update { true }
        }

        val isReady = combine(
            mainModelReady.asStateFlow(),
            settingsModelReady
        ) { values ->
            values.all { it }
        }

        viewModelScope.launch(Dispatchers.Main) {
            isReady.first { bool ->
                if (bool) {
                    _isReady.update {
                        true
                    }
                }
                bool
            }
        }
    }

    private fun handleLanguageUpdate(event: MainEvent.OnChangeLanguage) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            changeLanguage.execute(event.value)
            updateStateWithSavedHandle {
                it.copy(language = event.value)
            }
        }
    }

    private fun handleBrowseIncludedFilterItemUpdate(
        event: MainEvent.OnChangeBrowseIncludedFilterItem
    ) {
        val set = _state.value.browseIncludedFilterItems.toMutableSet()
        if (!set.add(event.value)) {
            set.remove(event.value)
        }
        handleDatastoreUpdate(
            key = DataStoreConstants.BROWSE_INCLUDED_FILTER_ITEMS,
            value = set,
            updateState = {
                it.copy(browseIncludedFilterItems = toList())
            }
        )
    }

    private fun handleBrowsePinnedPathsUpdate(
        event: MainEvent.OnChangeBrowsePinnedPaths
    ) {
        val set = _state.value.browsePinnedPaths.toMutableSet()
        if (!set.add(event.value)) {
            set.remove(event.value)
        }
        handleDatastoreUpdate(
            key = DataStoreConstants.BROWSE_PINNED_PATHS,
            value = set,
            updateState = {
                it.copy(browsePinnedPaths = toList())
            }
        )
    }

    /**
     * Handles and updates Datastore.
     */
    private fun <V> handleDatastoreUpdate(
        key: Preferences.Key<V>,
        value: V,
        updateState: V.(MainState) -> MainState
    ) {
        viewModelScope.launch(Dispatchers.Main.immediate) {
            setDatastore.execute(key = key, value = value)
            updateStateWithSavedHandle {
                value.updateState(it)
            }
        }
    }

    /**
     * Updates [MainState] along with [SavedStateHandle].
     */
    private suspend fun updateStateWithSavedHandle(
        function: (MainState) -> MainState
    ) {
        withContext(Dispatchers.Main.immediate) {
            _state.update {
                stateHandle[Constants.provideMainState()] = function(it)
                function(it)
            }
        }
    }

    private suspend inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        mutex.withLock {
            yield()
            this.value = function(this.value)
        }
    }
}