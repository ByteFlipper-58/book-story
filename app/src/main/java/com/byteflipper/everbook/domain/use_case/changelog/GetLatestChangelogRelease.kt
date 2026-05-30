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

class GetLatestChangelogRelease @Inject constructor(
    private val repository: ChangelogRepository
) {

    suspend fun execute(language: String): ChangelogRelease? {
        return repository.getLatestRelease(language)
    }
}
