package com.byteflipper.everbook.presentation.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

@Immutable
sealed class MainEvent {
    data class OnInit(
        val libraryViewModelReady: StateFlow<Boolean>,
        val settingsViewModelReady: StateFlow<Boolean>
    ) : MainEvent()

    data class OnChangeLanguage(val value: String) : MainEvent()
    data class OnChangeTheme(val value: String) : MainEvent()
    data class OnChangeDarkTheme(val value: String) : MainEvent()
    data class OnChangePureDark(val value: String) : MainEvent()
    data class OnChangeThemeContrast(val value: String) : MainEvent()
    data class OnChangeFontFamily(val value: String) : MainEvent()
    data class OnChangeFontStyle(val value: Boolean) : MainEvent()
    data class OnChangeFontSize(val value: Int) : MainEvent()
    data class OnChangeLineHeight(val value: Int) : MainEvent()
    data class OnChangeParagraphHeight(val value: Int) : MainEvent()
    data class OnChangeParagraphIndentation(val value: Int) : MainEvent()
    data class OnChangeShowStartScreen(val value: Boolean) : MainEvent()
    data class OnChangeCheckForUpdates(val value: Boolean) : MainEvent()
    data class OnChangeSidePadding(val value: Int) : MainEvent()
    data class OnChangeDoubleClickTranslation(val value: Boolean) : MainEvent()
    data class OnChangeFastColorPresetChange(val value: Boolean) : MainEvent()
    data class OnChangeBrowseFilesStructure(val value: String) : MainEvent()
    data class OnChangeBrowseLayout(val value: String) : MainEvent()
    data class OnChangeBrowseAutoGridSize(val value: Boolean) : MainEvent()
    data class OnChangeBrowseGridSize(val value: Int) : MainEvent()
    data class OnChangeBrowsePinFavoriteDirectories(val value: Boolean) : MainEvent()
    data class OnChangeBrowseSortOrder(val value: String) : MainEvent()
    data class OnChangeBrowseSortOrderDescending(val value: Boolean) : MainEvent()
    data class OnChangeBrowseIncludedFilterItem(val value: String) : MainEvent()
    data class OnChangeTextAlignment(val value: String) : MainEvent()
    data class OnChangeDoublePressExit(val value: Boolean) : MainEvent()
    data class OnChangeLetterSpacing(val value: Int) : MainEvent()
    data class OnChangeAbsoluteDark(val value: Boolean) : MainEvent()
    data class OnChangeCutoutPadding(val value: Boolean) : MainEvent()
    data class OnChangeFullscreen(val value: Boolean) : MainEvent()
    data class OnChangeKeepScreenOn(val value: Boolean) : MainEvent()
    data class OnChangeVerticalPadding(val value: Int) : MainEvent()
    data class OnChangeHideBarsOnFastScroll(val value: Boolean) : MainEvent()
    data class OnChangePerceptionExpander(val value: Boolean) : MainEvent()
    data class OnChangePerceptionExpanderPadding(val value: Int) : MainEvent()
    data class OnChangePerceptionExpanderThickness(val value: Int) : MainEvent()
    data class OnChangeCheckForTextUpdate(val value: Boolean) : MainEvent()
    data class OnChangeCheckForTextUpdateToast(val value: Boolean) : MainEvent()
    data class OnChangeScreenOrientation(val value: String) : MainEvent()
    data class OnChangeCustomScreenBrightness(val value: Boolean) : MainEvent()
    data class OnChangeScreenBrightness(val value: Float) : MainEvent()
}