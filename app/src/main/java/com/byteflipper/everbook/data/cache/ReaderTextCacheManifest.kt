/*
 * EverBook - a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.cache

internal data class ReaderTextCacheManifest(
    val schemaVersion: Int = 0,
    val parserVersion: Int = 0,
    val path: String? = null,
    val size: Long = -1,
    val lastModified: Long = -1,
    val chunkFiles: List<String>? = null
)

internal data class ReaderTextCacheEntry(
    val type: String? = null,
    val text: String? = null,
    val nested: Boolean? = null,
    val imageFile: String? = null
)
