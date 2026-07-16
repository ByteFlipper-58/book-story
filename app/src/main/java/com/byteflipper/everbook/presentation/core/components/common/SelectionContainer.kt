/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

@file:Suppress("DEPRECATION")

package com.byteflipper.everbook.presentation.core.components.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import com.byteflipper.everbook.R


private const val MENU_ITEM_COPY = 0
private const val MENU_ITEM_SHARE = 1
private const val MENU_ITEM_WEB = 2
private const val MENU_ITEM_TRANSLATE = 3
private const val MENU_ITEM_DICTIONARY = 4
private const val MENU_ITEM_BOOKMARK = 5
private const val MENU_ITEM_HIGHLIGHT = 6
private const val MENU_ITEM_NOTE = 7

/**
 * Text ActionMode callback.
 * Used in pair with [SelectionToolbar]. Follow [TextToolbar] for more info.
 */
private class TextActionModeCallback(
    private val context: Context,
    var rect: Rect = Rect.Zero,
    var onCopyRequested: (() -> Unit)? = null,
    var onShareRequested: (() -> Unit)? = null,
    var onWebSearchRequested: (() -> Unit)? = null,
    var onTranslateRequested: (() -> Unit)? = null,
    var onDictionaryRequested: (() -> Unit)? = null,
    var onBookmarkRequested: (() -> Unit)? = null,
    var onHighlightRequested: (() -> Unit)? = null,
    var onNoteRequested: (() -> Unit)? = null,
    var onDestroyActionModeCallback: (() -> Unit)? = null
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu)
        requireNotNull(mode)
        populateMenu(menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu)
        menu.clear()
        populateMenu(menu)
        return true
    }

    private fun populateMenu(menu: Menu) {
        onCopyRequested?.let {
            menu.add(0, MENU_ITEM_COPY, 0, context.getString(R.string.copy))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        onBookmarkRequested?.let {
            menu.add(0, MENU_ITEM_BOOKMARK, 1, context.getString(R.string.add_to_bookmarks))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        onHighlightRequested?.let {
            menu.add(0, MENU_ITEM_HIGHLIGHT, 2, context.getString(R.string.highlight))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        onNoteRequested?.let {
            menu.add(0, MENU_ITEM_NOTE, 3, context.getString(R.string.add_note))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        onShareRequested?.let {
            menu.add(0, MENU_ITEM_SHARE, 1, context.getString(R.string.share))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        onWebSearchRequested?.let {
            menu.add(0, MENU_ITEM_WEB, 2, context.getString(R.string.web_search))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        onTranslateRequested?.let {
            menu.add(0, MENU_ITEM_TRANSLATE, 3, context.getString(R.string.translate))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }

        onDictionaryRequested?.let {
            menu.add(0, MENU_ITEM_DICTIONARY, 4, context.getString(R.string.dictionary))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val itemId = item?.itemId ?: return false
        when (item!!.itemId) {
            MENU_ITEM_COPY -> onCopyRequested?.invoke()
            MENU_ITEM_SHARE -> onShareRequested?.invoke()
            MENU_ITEM_WEB -> onWebSearchRequested?.invoke()
            MENU_ITEM_TRANSLATE -> onTranslateRequested?.invoke()
            MENU_ITEM_DICTIONARY -> onDictionaryRequested?.invoke()
            MENU_ITEM_BOOKMARK -> onBookmarkRequested?.invoke()
            MENU_ITEM_HIGHLIGHT -> onHighlightRequested?.invoke()
            MENU_ITEM_NOTE -> onNoteRequested?.invoke()
            else -> return false
        }
        mode?.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroyActionModeCallback?.invoke()
    }
}

/**
 * Floating [TextActionModeCallback].
 */
private class FloatingTextActionModeCallback(
    val callback: TextActionModeCallback
) : ActionMode.Callback2() {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return callback.onActionItemClicked(mode, item)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return callback.onPrepareActionMode(mode, menu)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        callback.onDestroyActionMode(mode)
    }

    override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: android.graphics.Rect?) {
        val rect = callback.rect
        outRect?.set(
            rect.left.toInt(),
            rect.top.toInt(),
            rect.right.toInt(),
            rect.bottom.toInt()
        )
    }
}

/**
 * Selection Toolbar.
 * Used in pair with [SelectionContainer] to display custom toolbar.
 */
private class SelectionToolbar(
    private val view: View,
    context: Context,
    private val onCopyRequest: (() -> Unit)?,
    private val onShareRequest: ((String) -> Unit)?,
    private val onWebSearchRequest: ((String) -> Unit)?,
    private val onTranslateRequest: ((String) -> Unit)?,
    private val onDictionaryRequest: ((String) -> Unit)?,
    private val onBookmarkRequest: ((String) -> Unit)?,
    private val onHighlightRequest: ((String, Rect) -> Unit)?,
    private val onNoteRequest: ((String) -> Unit)?,
) : TextToolbar {
    private var actionMode: ActionMode? = null
    private val callback = TextActionModeCallback(
        context = context,
        onDestroyActionModeCallback = {
            actionMode = null
            status = TextToolbarStatus.Hidden
        }
    )

    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override var status: TextToolbarStatus by mutableStateOf(TextToolbarStatus.Hidden)

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        callback.rect = rect
        callback.onCopyRequested = {
            onCopyRequested?.invoke()
            onCopyRequest?.invoke()
        }
        callback.onShareRequested = selectedTextAction(onCopyRequested, onShareRequest)
        callback.onWebSearchRequested = selectedTextAction(onCopyRequested, onWebSearchRequest)
        callback.onTranslateRequested = selectedTextAction(onCopyRequested, onTranslateRequest)
        callback.onDictionaryRequested = selectedTextAction(onCopyRequested, onDictionaryRequest)
        callback.onBookmarkRequested = selectedTextAction(onCopyRequested, onBookmarkRequest)
        callback.onHighlightRequested = selectedTextActionWithRect(onCopyRequested, onHighlightRequest)
        callback.onNoteRequested = selectedTextAction(onCopyRequested, onNoteRequest)

        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = view.startActionMode(
                FloatingTextActionModeCallback(callback),
                ActionMode.TYPE_FLOATING
            )
        } else {
            actionMode?.invalidate()
        }
    }

    private fun selectedTextAction(
        onCopyRequested: (() -> Unit)?,
        onTextAction: ((String) -> Unit)?
    ): (() -> Unit)? {
        onTextAction ?: return null
        return {
            val previousClipboard = clipboardManager.primaryClip
            onCopyRequested?.invoke()
            val currentClipboard = clipboardManager.text

            onTextAction.invoke(currentClipboard.toString())

            if (previousClipboard != null) {
                clipboardManager.setPrimaryClip(previousClipboard)
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, " "))
            }
        }
    }

    private fun selectedTextActionWithRect(
        onCopyRequested: (() -> Unit)?,
        onTextAction: ((String, Rect) -> Unit)?
    ): (() -> Unit)? {
        onTextAction ?: return null
        return {
            val previousClipboard = clipboardManager.primaryClip
            onCopyRequested?.invoke()
            val selectedText = clipboardManager.text.toString()
            onTextAction(selectedText, callback.rect)
            if (previousClipboard != null) {
                clipboardManager.setPrimaryClip(previousClipboard)
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, " "))
            }
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }
}

/**
 * Selection container.
 *
 * Requires [androidx.compose.foundation.ComposeFoundationFlags.isNewContextMenuEnabled] to be
 * false so Compose routes selection through [LocalTextToolbar] rather than the platform context
 * menu (Copy / Share only).
 */
@Composable
fun SelectionContainer(
    onCopyRequested: (() -> Unit),
    onShareRequested: ((String) -> Unit),
    onWebSearchRequested: ((String) -> Unit),
    onTranslateRequested: ((String) -> Unit),
    onDictionaryRequested: ((String) -> Unit),
    onBookmarkRequested: ((String) -> Unit)?,
    onHighlightRequested: ((String, Rect) -> Unit)?,
    onNoteRequested: ((String) -> Unit)?,
    content: @Composable (toolbarHidden: Boolean) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current

    val selectionToolbar = remember(
        view,
        context,
        onCopyRequested,
        onShareRequested,
        onWebSearchRequested,
        onTranslateRequested,
        onDictionaryRequested,
        onBookmarkRequested,
        onHighlightRequested,
        onNoteRequested
    ) {
        SelectionToolbar(
            view = view,
            context = context,
            onCopyRequest = { onCopyRequested() },
            onShareRequest = { onShareRequested(it) },
            onWebSearchRequest = { onWebSearchRequested(it) },
            onTranslateRequest = { onTranslateRequested(it) },
            onDictionaryRequest = { onDictionaryRequested(it) },
            onBookmarkRequest = onBookmarkRequested,
            onHighlightRequest = onHighlightRequested,
            onNoteRequest = onNoteRequested
        )
    }
    val isToolbarHidden = remember(selectionToolbar.status) {
        derivedStateOf {
            selectionToolbar.status == TextToolbarStatus.Hidden
        }
    }

    CompositionLocalProvider(
        LocalTextToolbar provides selectionToolbar
    ) {
        androidx.compose.foundation.text.selection.SelectionContainer {
            content(isToolbarHidden.value)
        }
    }
}
