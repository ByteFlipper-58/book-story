/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.ui.main

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.domain.library.book.SelectableNullableBook

@Immutable
sealed class ExternalImportEvent {
    data class OnHandleUris(val uris: List<Uri>) : ExternalImportEvent()
    data object OnDismissAddDialog : ExternalImportEvent()
    data object OnActionAddDialog : ExternalImportEvent()
    data class OnSelectAddDialog(val book: SelectableNullableBook) : ExternalImportEvent()
}
