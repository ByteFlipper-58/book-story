/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import androidx.activity.ComponentActivity
import com.byteflipper.everbook.domain.distribution.StoreUpdateController
import javax.inject.Inject

class EverbookStoreUpdateController @Inject constructor() : StoreUpdateController {
    override fun onActivityReady(activity: ComponentActivity) = Unit

    override fun onReaderOpened(activity: ComponentActivity) = Unit

    override val supportsInAppReview: Boolean = false

    override fun requestReview(activity: ComponentActivity) = Unit
}
