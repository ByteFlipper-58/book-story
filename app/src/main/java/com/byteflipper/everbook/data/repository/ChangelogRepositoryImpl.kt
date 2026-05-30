/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import android.app.Application
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.domain.repository.ChangelogRepository
import com.byteflipper.everbook.domain.changelog.ChangelogBlock
import com.byteflipper.everbook.domain.changelog.ChangelogPage
import com.byteflipper.everbook.domain.changelog.ChangelogRelease
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangelogRepositoryImpl @Inject constructor(
    private val app: Application
) : ChangelogRepository {

    private val gson = Gson()

    override suspend fun getReleases(language: String): List<ChangelogRelease> {
        return withContext(Dispatchers.IO) {
            val index = readAsset(INDEX_PATH)?.let {
                runCatching {
                    gson.fromJson(it, ChangelogIndexDto::class.java)
                }.getOrNull()
            } ?: return@withContext emptyList()

            index.releases
                .mapNotNull { entry ->
                    entry.toRelease(language)
                }
                .sortedByDescending { it.versionCode }
        }
    }

    override suspend fun getLatestRelease(language: String): ChangelogRelease? {
        return getReleases(language).firstOrNull()
    }

    private fun ChangelogReleaseDto.toRelease(language: String): ChangelogRelease? {
        val fileBase = file.ifBlank { versionName }.toChangelogFileBase()
        val availableLocales = provideAvailableLocales(fileBase)
        if (availableLocales.isEmpty()) return null

        val candidates = provideLanguageCandidates(language)

        val content = (candidates + availableLocales)
            .distinct()
            .firstNotNullOfOrNull { locale ->
                if (locale !in availableLocales) return@firstNotNullOfOrNull null

                readAsset("$BASE_PATH/${fileBase.toLocalizedFileName(locale)}")?.let { markdown ->
                    locale to markdown
                }
            } ?: return null

        return ChangelogRelease(
            versionCode = versionCode,
            versionName = versionName,
            file = fileBase,
            locale = content.first,
            availableLocales = availableLocales,
            pages = parsePages(content.second)
        )
    }

    private fun parsePages(markdown: String): List<ChangelogPage> {
        val normalized = markdown
            .replace("\r\n", "\n")
            .replace('\r', '\n')

        return normalized
            .split(Regex("(?m)^\\s*---\\s*$"))
            .mapNotNull { section ->
                parsePage(section)
            }
            .ifEmpty {
                listOf(
                    ChangelogPage(
                        title = "",
                        blocks = parseBlocks(normalized.lines())
                    )
                )
            }
    }

    private fun parsePage(section: String): ChangelogPage? {
        val lines = section
            .trim()
            .lines()
            .dropWhile { it.isBlank() }

        if (lines.isEmpty()) return null

        val firstLine = lines.first().trim()
        val title = if (firstLine.startsWith("#")) {
            firstLine.dropWhile { it == '#' }.trim()
        } else {
            ""
        }
        val bodyLines = if (title.isNotEmpty()) lines.drop(1) else lines

        return ChangelogPage(
            title = title,
            blocks = parseBlocks(bodyLines)
        )
    }

    private fun parseBlocks(lines: List<String>): List<ChangelogBlock> {
        val blocks = mutableListOf<ChangelogBlock>()
        val paragraph = mutableListOf<String>()
        val bullets = mutableListOf<String>()

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += ChangelogBlock.Paragraph(
                    paragraph.joinToString(" ").clearMarkdown()
                )
                paragraph.clear()
            }
        }

        fun flushBullets() {
            if (bullets.isNotEmpty()) {
                blocks += ChangelogBlock.BulletList(bullets.toList())
                bullets.clear()
            }
        }

        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> {
                    flushParagraph()
                    flushBullets()
                }

                trimmed.startsWith("##") -> {
                    flushParagraph()
                    flushBullets()
                    blocks += ChangelogBlock.Heading(
                        trimmed.dropWhile { it == '#' }.clearAllMarkdown()
                    )
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    flushParagraph()
                    bullets += trimmed.drop(2).clearMarkdown()
                }

                trimmed.startsWith(">") -> {
                    flushParagraph()
                    flushBullets()
                    blocks += ChangelogBlock.Quote(trimmed.drop(1).clearMarkdown())
                }

                else -> {
                    flushBullets()
                    paragraph += trimmed
                }
            }
        }

        flushParagraph()
        flushBullets()

        return blocks
    }

    private fun provideLanguageCandidates(language: String): List<String> {
        val normalized = language
            .replace('_', '-')
            .lowercase(Locale.ROOT)
            .ifBlank { DEFAULT_LANGUAGE }
        val base = normalized.substringBefore("-")

        return listOf(normalized, base, DEFAULT_LANGUAGE).distinct()
    }

    private fun provideAvailableLocales(fileBase: String): List<String> {
        val prefix = "$fileBase."

        return listAssets(BASE_PATH)
            .mapNotNull { assetName ->
                if (
                    !assetName.startsWith(prefix) ||
                    !assetName.endsWith(MARKDOWN_EXTENSION)
                ) {
                    return@mapNotNull null
                }

                assetName
                    .removePrefix(prefix)
                    .removeSuffix(MARKDOWN_EXTENSION)
                    .takeIf { it.isNotBlank() }
            }
            .distinct()
            .sortedWith(
                compareBy<String> {
                    LANGUAGE_ORDER.indexOf(it).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
                }.thenBy { it }
            )
    }

    private fun listAssets(path: String): List<String> {
        return try {
            app.assets.list(path)?.toList().orEmpty()
        } catch (_: IOException) {
            emptyList()
        }
    }

    private fun readAsset(path: String): String? {
        return try {
            app.assets.open(path).bufferedReader().use { it.readText() }
        } catch (_: IOException) {
            null
        }
    }

    private fun String.toLocalizedFileName(locale: String): String {
        return "$this.$locale$MARKDOWN_EXTENSION"
    }

    private fun String.toChangelogFileBase(): String {
        return removeSuffix(MARKDOWN_EXTENSION)
    }

    private fun String.clearMarkdown(): String {
        return replace(Regex("(_+)|(\\*+)"), "")
    }

    private fun String.clearAllMarkdown(): String {
        return replace(Regex("(_+)|(\\*+)|(#+)"), "").trim()
    }

    private data class ChangelogIndexDto(
        val releases: List<ChangelogReleaseDto> = emptyList()
    )

    private data class ChangelogReleaseDto(
        val versionCode: Int = 0,
        val versionName: String = "",
        val file: String = ""
    )

    private companion object {
        const val BASE_PATH = "changelog"
        const val INDEX_PATH = "$BASE_PATH/index.json"
        const val DEFAULT_LANGUAGE = "en"
        const val MARKDOWN_EXTENSION = ".md"
        val LANGUAGE_ORDER = listOf(
            "en",
            "uk",
            "de",
            "ar",
            "es",
            "tr",
            "fr",
            "pl",
            "ru",
            "it",
            "zh",
            "hi",
            "pt"
        )
    }
}
