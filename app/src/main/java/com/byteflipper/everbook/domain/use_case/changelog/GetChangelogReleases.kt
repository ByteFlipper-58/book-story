/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.changelog

import com.byteflipper.everbook.domain.repository.ChangelogRepository
import com.byteflipper.everbook.domain.changelog.ChangelogRelease
import javax.inject.Inject

class GetChangelogReleases @Inject constructor(
    private val repository: ChangelogRepository
) {

    suspend fun execute(language: String): List<ChangelogRelease> {
        return repository.getReleases(language).take(MAX_AVAILABLE_RELEASES)
    }

    private companion object {
        const val MAX_AVAILABLE_RELEASES = 3
    }
}
