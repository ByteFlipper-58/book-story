/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.font

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.byteflipper.everbook.domain.reader.FontWithName
import com.byteflipper.everbook.domain.ui.UIText
import java.io.File
import java.util.UUID

/** Stores user fonts privately so they remain available after the source document is removed. */
object CustomFontStore {
    private const val DIRECTORY = "reader_fonts"
    private const val PREFIX = "custom:"
    private val supportedExtensions = setOf("ttf", "otf")

    fun list(context: Context): List<FontWithName> = directory(context)
        .listFiles()
        .orEmpty()
        .sortedBy { it.nameWithoutExtension.lowercase() }
        .mapNotNull { file ->
            runCatching {
                FontWithName(
                    id = PREFIX + file.name,
                    fontName = UIText.StringValue(displayName(file)),
                    font = FontFamily(Typeface.createFromFile(file))
                )
            }.getOrNull()
        }

    fun import(context: Context, source: android.net.Uri): FontWithName? = runCatching {
        val extension = context.contentResolver.getType(source)
            ?.substringAfterLast('/', "")
            ?.lowercase()
            ?.takeIf { it in supportedExtensions }
            ?: source.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
                ?.takeIf { it in supportedExtensions }
            ?: return null
        val requestedName = source.lastPathSegment
            ?.substringBeforeLast('.')
            ?.replace(Regex("[^A-Za-z0-9._ -]"), "_")
            ?.take(80)
            ?.ifBlank { "Custom font" }
            ?: "Custom font"
        val file = File(directory(context), "$requestedName-${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(source)?.use { input ->
            file.outputStream().use(input::copyTo)
        } ?: return null
        val typeface = Typeface.createFromFile(file)
        FontWithName(PREFIX + file.name, UIText.StringValue(displayName(file)), FontFamily(typeface))
    }.getOrNull()

    fun delete(context: Context, id: String) {
        if (!id.startsWith(PREFIX)) return
        File(directory(context), id.removePrefix(PREFIX)).takeIf { it.parentFile == directory(context) }?.delete()
    }

    private fun directory(context: Context): File = File(context.filesDir, DIRECTORY).apply { mkdirs() }

    private fun displayName(file: File): String = FontNameReader.displayName(file)
        ?: file.nameWithoutExtension.replace(Regex("-[0-9a-f]{8}-[0-9a-f-]{27}$"), "")
}
