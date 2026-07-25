/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("UnusedVariable", "unused")

package com.byteflipper.everbook.ui.main

import android.annotation.SuppressLint
import android.app.assist.AssistContent
import android.content.Intent
import android.database.CursorWindow
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.internal.immutableListOf
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.assist.ReadingContextTracker
import com.byteflipper.everbook.domain.distribution.ReaderEntryActionController
import com.byteflipper.everbook.domain.distribution.StoreUpdateController
import com.byteflipper.everbook.domain.navigator.NavigatorItem
import com.byteflipper.everbook.domain.navigator.StackEvent
import com.byteflipper.everbook.presentation.browse.BrowseAddDialog
import com.byteflipper.everbook.presentation.core.components.navigation_bar.NavigationBar
import com.byteflipper.everbook.presentation.core.components.navigation_rail.NavigationRail
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.main.MainActivityKeyboardManager
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.presentation.navigator.Navigator
import com.byteflipper.everbook.presentation.navigator.NavigatorTabs
import com.byteflipper.everbook.ui.browse.BrowseModel
import com.byteflipper.everbook.ui.browse.BrowseScreen
import com.byteflipper.everbook.ui.history.HistoryEvent
import com.byteflipper.everbook.ui.history.HistoryModel
import com.byteflipper.everbook.ui.history.HistoryScreen
import com.byteflipper.everbook.ui.library.CategoriesModel
import com.byteflipper.everbook.ui.library.LibraryEvent
import com.byteflipper.everbook.ui.library.LibraryModel
import com.byteflipper.everbook.ui.library.LibraryScreen
import com.byteflipper.everbook.ui.reader.ReaderScreen
import com.byteflipper.everbook.ui.settings.SettingsModel
import com.byteflipper.everbook.ui.start.StartScreen
import com.byteflipper.everbook.ui.theme.BookStoryTheme
import com.byteflipper.everbook.ui.theme.Transitions
import com.byteflipper.everbook.ui.changelog.ChangelogScreen
import com.byteflipper.everbook.domain.ui.isDark
import com.byteflipper.everbook.domain.ui.isPureDark
import org.json.JSONObject
import java.lang.reflect.Field
import javax.inject.Inject
import kotlin.math.roundToInt


@SuppressLint("DiscouragedPrivateApi")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var readerEntryActionController: ReaderEntryActionController

    @Inject
    lateinit var storeUpdateController: StoreUpdateController

    @Inject
    lateinit var readingContextTracker: ReadingContextTracker

    // Creating an instance of Models
    private val mainModel: MainModel by viewModels()
    private val settingsModel: SettingsModel by viewModels()
    private val categoriesModel: CategoriesModel by viewModels()
    private val externalImportModel: ExternalImportModel by viewModels()

    // Book ids requested from outside the app (App Functions, shortcuts). Buffered, because the
    // intent is handled before the Navigator that consumes it enters composition.
    private val openBookChannel = Channel<Int>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !mainModel.isReady.value || !categoriesModel.isReady.value
            }
        }

        // Default super
        super.onCreate(savedInstanceState)

        readerEntryActionController.configure(this)
        // storeUpdateController.onActivityReady is triggered later, after onboarding (see below),
        // so the update / consent dialogs don't interrupt the start screen.

        // Bigger Cursor size for Room
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initializing the MainModel
        mainModel.init(settingsModel.isReady)

        // Edge to edge
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Initializing Screen Models
            val libraryModel = hiltViewModel<LibraryModel>()
            val historyModel = hiltViewModel<HistoryModel>()
            val browseModel = hiltViewModel<BrowseModel>()

            val state = mainModel.state.collectAsStateWithLifecycle()
            val isLoaded = mainModel.isReady.collectAsStateWithLifecycle()
            val pendingChangelogRelease by mainModel.pendingChangelogRelease
                .collectAsStateWithLifecycle()
            val categoriesReady = categoriesModel.isReady.collectAsStateWithLifecycle()

            val tabs = immutableListOf(
                NavigatorItem(
                    screen = LibraryScreen,
                    title = R.string.library_screen,
                    tooltip = R.string.library_content_desc,
                    selectedIcon = R.drawable.ic_menu_book_rounded_24px,
                    unselectedIcon = R.drawable.ic_menu_book_rounded_24px
                ),
                NavigatorItem(
                    screen = HistoryScreen,
                    title = R.string.history_screen,
                    tooltip = R.string.history_content_desc,
                    selectedIcon = R.drawable.ic_history_rounded_24px,
                    unselectedIcon = R.drawable.ic_history_rounded_24px
                ),
                NavigatorItem(
                    screen = BrowseScreen,
                    title = R.string.browse_screen,
                    tooltip = R.string.browse_content_desc,
                    selectedIcon = R.drawable.ic_explore_rounded_24px,
                    unselectedIcon = R.drawable.ic_explore_rounded_24px
                )
            )

            MainActivityKeyboardManager()

            if (isLoaded.value && categoriesReady.value) {
                BookStoryTheme(
                    theme = state.value.theme,
                    isDark = state.value.darkTheme.isDark(),
                    isPureDark = state.value.pureDark.isPureDark(this),
                    themeContrast = state.value.themeContrast
                ) {
                    Navigator(
                        initialScreen = if (state.value.showStartScreen) StartScreen
                        else LibraryScreen,
                        transitionSpec = { lastEvent ->
                            when (lastEvent) {
                                StackEvent.Default -> {
                                    Transitions.SlidingTransitionIn
                                        .togetherWith(Transitions.SlidingTransitionOut)
                                }

                                StackEvent.Pop -> {
                                    Transitions.BackSlidingTransitionIn
                                        .togetherWith(Transitions.BackSlidingTransitionOut)
                                }
                            }
                        },
                        contentKey = {
                            when (it) {
                                LibraryScreen, HistoryScreen, BrowseScreen -> "tabs"
                                else -> it
                            }
                        },
                        backHandlerEnabled = { it != StartScreen }
                    ) { screen ->
                        val navigator = LocalNavigator.current
                        val externalImportState by externalImportModel.state
                            .collectAsStateWithLifecycle()

                        LaunchedEffect(navigator) {
                            externalImportModel.openBookChannel.receiveAsFlow().collectLatest { bookId ->
                                libraryModel.refresh()
                                navigator.push(ReaderScreen(bookId))
                            }
                        }

                        LaunchedEffect(navigator) {
                            openBookChannel.receiveAsFlow().collectLatest { bookId ->
                                navigator.push(ReaderScreen(bookId))
                            }
                        }

                        LaunchedEffect(navigator, pendingChangelogRelease) {
                            pendingChangelogRelease?.let { release ->
                                mainModel.onEvent(
                                    MainEvent.OnChangeChangelogLastSeenVersionCode(
                                        release.versionCode
                                    )
                                )
                                mainModel.consumePendingChangelogRelease()
                                navigator.push(ChangelogScreen(release.versionCode))
                            }
                        }

                        LaunchedEffect(navigator) {
                            externalImportModel.booksAddedChannel.receiveAsFlow().collectLatest {
                                navigator.push(
                                    LibraryScreen,
                                    popping = true,
                                    saveInBackStack = false
                                )
                                libraryModel.refresh()
                                LibraryScreen.scrollToPageCompositionChannel.trySend(0)
                                getString(R.string.books_added).showToast(this@MainActivity)
                            }
                        }

                        LaunchedEffect(historyModel, navigator) {
                            historyModel.openLatestBookChannel.receiveAsFlow().collectLatest {
                                HistoryScreen.insertHistoryChannel.trySend(it)
                                navigator.push(ReaderScreen(it))
                            }
                        }

                        LaunchedEffect(Unit) {
                            externalImportModel.importFailedChannel.receiveAsFlow().collectLatest {
                                getString(R.string.error_something_went_wrong)
                                    .showToast(this@MainActivity)
                            }
                        }

                        LaunchedEffect(Unit) {
                            externalImportModel.largePdfNoticeChannel.receiveAsFlow().collectLatest {
                                getString(R.string.pdf_too_large_native_only)
                                    .showToast(this@MainActivity)
                            }
                        }

                        LaunchedEffect(screen) {
                            if (screen is ReaderScreen) {
                                readerEntryActionController.onReaderEntered(this@MainActivity)
                                storeUpdateController.onReaderOpened(this@MainActivity)
                            }
                        }

                        // Check for app updates only after onboarding so the UMP / update
                        // dialogs never appear on the start screen.
                        LaunchedEffect(screen) {
                            if (screen != StartScreen && !state.value.showStartScreen) {
                                storeUpdateController.onActivityReady(this@MainActivity)
                            }
                        }

                        if (externalImportState.showAddDialog) {
                            BrowseAddDialog(
                                loadingAddDialog = externalImportState.loadingAddDialog,
                                selectedBooksAddDialog = externalImportState.booksAddDialog,
                                dismissAddDialog = {
                                    externalImportModel.onEvent(
                                        ExternalImportEvent.OnDismissAddDialog
                                    )
                                },
                                actionAddDialog = {
                                    externalImportModel.onEvent(
                                        ExternalImportEvent.OnActionAddDialog
                                    )
                                },
                                selectAddDialog = { event ->
                                    externalImportModel.onEvent(
                                        ExternalImportEvent.OnSelectAddDialog(event.book)
                                    )
                                },
                                navigateToLibrary = {
                                    navigator.push(
                                        LibraryScreen,
                                        popping = true,
                                        saveInBackStack = false
                                    )
                                }
                            )
                        }

                        when (screen) {
                            LibraryScreen, HistoryScreen, BrowseScreen -> {
                                NavigatorTabs(
                                    currentTab = screen,
                                    transitionSpec = {
                                        Transitions.FadeTransitionIn
                                            .togetherWith(Transitions.FadeTransitionOut)
                                    },
                                    navigationBar = {
                                        NavigationBar(
                                            tabs = tabs,
                                            onTabReselected = {
                                                if (it.screen == HistoryScreen) {
                                                    historyModel.onEvent(
                                                        HistoryEvent.OnOpenLatestBookFromHistory
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    navigationRail = {
                                        NavigationRail(
                                            tabs = tabs,
                                            onTabReselected = {
                                                if (it.screen == HistoryScreen) {
                                                    historyModel.onEvent(
                                                        HistoryEvent.OnOpenLatestBookFromHistory
                                                    )
                                                }
                                            }
                                        )
                                    }
                                ) { tab ->
                                    tab.Content()
                                }
                            }

                            else -> {
                                screen.Content()
                            }
                        }
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            handleIncomingIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onDestroy() {
        cacheDir.deleteRecursively()
        super.onDestroy()
    }

    /**
     * Tells the system assistant what the user is reading right now, so a question asked over the
     * reader ("who is this character?", "translate this") arrives with the book in context.
     *
     * Only sent while the reader is open, and only as much as needed: title, author, position and a
     * short excerpt around the reading position (see [ReadingContextTracker]). Nothing is sent when
     * the user turned the assistant reading context off in the settings.
     */
    override fun onProvideAssistContent(outContent: AssistContent?) {
        super.onProvideAssistContent(outContent)
        if (outContent == null) return

        if (!mainModel.state.value.assistantReadingContext) return
        val reading = readingContextTracker.current.value ?: return

        outContent.structuredData = JSONObject().apply {
            put("@context", "https://schema.org")
            put("@type", "Book")
            put("name", reading.title)
            reading.author?.let { put("author", it) }
            put("readingProgressPercent", (reading.progress * 100).roundToInt().coerceIn(0, 100))
            reading.chapterTitle?.let { put("currentChapter", it) }
            reading.excerpt?.let { put("currentText", it) }
        }.toString()

        outContent.intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_BOOK
            putExtra(EXTRA_BOOK_ID, reading.bookId)
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val bookId = intent?.getIntExtra(EXTRA_BOOK_ID, NO_BOOK_ID) ?: NO_BOOK_ID
        if (bookId != NO_BOOK_ID) {
            openBookChannel.trySend(bookId)
            return
        }

        val uris = intent.extractBookUris()
        if (uris.isEmpty()) return

        externalImportModel.onEvent(ExternalImportEvent.OnHandleUris(uris))
    }

    private fun Intent?.extractBookUris(): List<Uri> {
        if (this == null) return emptyList()

        return buildList {
            when (action) {
                Intent.ACTION_VIEW -> data?.let(::add)
                Intent.ACTION_SEND -> {
                    IntentCompat.getParcelableExtra(
                        this@extractBookUris,
                        Intent.EXTRA_STREAM,
                        Uri::class.java
                    )?.let(::add)
                }

                Intent.ACTION_SEND_MULTIPLE -> {
                    IntentCompat.getParcelableArrayListExtra(
                        this@extractBookUris,
                        Intent.EXTRA_STREAM,
                        Uri::class.java
                    )?.let(::addAll)
                }
            }

            clipData?.let { data ->
                repeat(data.itemCount) { index ->
                    data.getItemAt(index).uri?.let(::add)
                }
            }
        }.distinct()
    }

    private fun LibraryModel.refresh() {
        onEvent(
            LibraryEvent.OnRefreshList(
                loading = false,
                hideSearch = false
            )
        )
    }

    companion object {
        /** Opens [EXTRA_BOOK_ID] straight in the reader. Used by App Functions. */
        const val ACTION_OPEN_BOOK = "com.byteflipper.everbook.action.OPEN_BOOK"

        /** Id of the book to open in the reader, see [ACTION_OPEN_BOOK]. */
        const val EXTRA_BOOK_ID = "com.byteflipper.everbook.extra.BOOK_ID"

        private const val NO_BOOK_ID = -1
    }
}
