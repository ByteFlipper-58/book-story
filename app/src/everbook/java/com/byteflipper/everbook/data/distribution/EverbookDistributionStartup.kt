/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.distribution

import com.byteflipper.everbook.domain.distribution.DistributionStartup
import javax.inject.Inject

class EverbookDistributionStartup @Inject constructor() : DistributionStartup {
    override fun onAppCreate() = Unit
}
