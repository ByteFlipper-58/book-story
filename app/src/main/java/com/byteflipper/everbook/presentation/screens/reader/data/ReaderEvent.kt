package com.byteflipper.everbook.presentation.screens.reader.data

import androidx.activity.ComponentActivity
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.model.Book
import com.byteflipper.everbook.domain.util.OnNavigate
import com.byteflipper.everbook.domain.util.UIText
import com.byteflipper.everbook.presentation.core.navigation.Screen

@Immutable
sealed class ReaderEvent {
    data class OnInit(
        val screen: Screen.Reader,
        val navigateBack: () -> Unit,
        val fullscreenMode: Boolean,
        val checkForTextUpdate: Boolean,
        val checkForTextUpdateToast: () -> Unit,
        val activity: ComponentActivity,
        val refreshList: (Book) -> Unit,
        val onError: (UIText) -> Unit
    ) : ReaderEvent()

    data class OnUpdateProgress(
        val refreshList: (Book) -> Unit
    ) : ReaderEvent()

    data object OnClearViewModel : ReaderEvent()

    data object OnTextIsEmpty : ReaderEvent()

    data class OnLoadText(
        val checkForUpdate: () -> Unit,
        val refreshList: (Book) -> Unit,
        val onError: (UIText) -> Unit,
        val onTextIsEmpty: () -> Unit
    ) : ReaderEvent()

    data class OnShowHideMenu(
        val show: Boolean? = null,
        val fullscreenMode: Boolean,
        val saveCheckpoint: Boolean,
        val activity: ComponentActivity
    ) : ReaderEvent()

    data class OnRestoreCheckpoint(
        val refreshList: (Book) -> Unit
    ) : ReaderEvent()

    data class OnGoBack(
        val context: ComponentActivity,
        val navigate: () -> Unit,
        val refreshList: (Book) -> Unit
    ) : ReaderEvent()

    data class OnChangeProgress(
        val progress: Float,
        val firstVisibleItemIndex: Int,
        val firstVisibleItemOffset: Int,
        val refreshList: (Book) -> Unit
    ) : ReaderEvent()

    data class OnScroll(
        val progress: Float
    ) : ReaderEvent()

    data class OnScrollToChapter(
        val chapterStartIndex: Int,
        val refreshList: (Book) -> Unit
    ) : ReaderEvent()

    data class OnShowHideSettingsBottomSheet(
        val show: Boolean
    ) : ReaderEvent()

    data class OnShowHideChaptersDrawer(
        val show: Boolean
    ) : ReaderEvent()

    data class OnScrollToSettingsPage(
        val page: Int,
        val pagerState: PagerState?
    ) : ReaderEvent()

    data class OnOpenTranslator(
        val textToTranslate: String,
        val translateWholeParagraph: Boolean,
        val context: ComponentActivity,
        val noAppsFound: () -> Unit
    ) : ReaderEvent()

    data class OnOpenShareApp(
        val textToShare: String,
        val context: ComponentActivity,
        val noAppsFound: () -> Unit
    ) : ReaderEvent()

    data class OnOpenWebBrowser(
        val textToSearch: String,
        val context: ComponentActivity,
        val noAppsFound: () -> Unit
    ) : ReaderEvent()

    data class OnOpenDictionary(
        val textToDefine: String,
        val context: ComponentActivity,
        val noAppsFound: () -> Unit
    ) : ReaderEvent()

    data class OnCheckTextForUpdate(
        val noUpdateFound: () -> Unit
    ) : ReaderEvent()

    data class OnShowHideUpdateDialog(
        val show: Boolean
    ) : ReaderEvent()

    data object OnCancelCheckTextForUpdate : ReaderEvent()

    data class OnUpdateText(
        val activity: ComponentActivity,
        val onNavigate: OnNavigate
    ) : ReaderEvent()
}