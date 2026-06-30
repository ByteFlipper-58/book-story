/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.domain.use_case.data_store

import androidx.datastore.preferences.core.Preferences
import com.byteflipper.everbook.domain.repository.DataStoreRepository
import javax.inject.Inject

class GetDatastore @Inject constructor(
    private val repository: DataStoreRepository
) {
    suspend fun <T> execute(key: Preferences.Key<T>): T? {
        return repository.getNullableDataFromDataStore(key)
    }
}
