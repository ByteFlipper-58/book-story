/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import androidx.activity.ComponentActivity
import com.byteflipper.everbook.domain.distribution.ReaderEntryActionController
import javax.inject.Inject

class EverbookReaderEntryActionController @Inject constructor() : ReaderEntryActionController {
    override fun configure(activity: ComponentActivity) = Unit

    override fun onReaderEntered(activity: ComponentActivity) = Unit
}
