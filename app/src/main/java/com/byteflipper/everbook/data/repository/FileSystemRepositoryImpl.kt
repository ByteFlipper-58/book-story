/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.domain.browse.SelectableFile
import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.file.CachedFileCompat
import com.byteflipper.everbook.domain.library.book.NullableBook
import com.byteflipper.everbook.domain.library.book.NullableBook.NotNull
import com.byteflipper.everbook.domain.library.book.NullableBook.Null
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import com.byteflipper.everbook.domain.ui.UIText
import com.byteflipper.everbook.presentation.core.constants.provideExtensions
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_BOOK_FROM_FILE = "BOOK FROM FILE, REPO"
private const val GET_FILES = "FILES, REPO"
private const val IMPORT_BOOK = "IMPORT BOOK, REPO"

/**
 * File System repository.
 * Manages all File System related work.
 */
@Singleton
class FileSystemRepositoryImpl @Inject constructor(
    private val application: Application,
    private val database: BookDao,
    private val fileParser: FileParser
) : FileSystemRepository {

    /**
     * Gets all matching files from device.
     * Filters by [query] and sorts out not supported file formats and already added files.
     */
    override suspend fun getFiles(query: String): List<SelectableFile> {
        Log.i(GET_FILES, "Getting files from device, query: \"$query\".")

        val existingPaths = database
            .searchBooks("")
            .map { it.filePath }
        val supportedExtensions = provideExtensions()

        /**
         * Verify that [CachedFile] is valid and can be shown correctly.
         */
        fun CachedFile.isValid(): Boolean {
            // First: Ensuring supported extension
            supportedExtensions.any { ext ->
                name.endsWith(ext, ignoreCase = true)
            }.let { if (!it) return false }

            // Second: Ensuring query to match
            if (query.isNotBlank()) {
                name.contains(query.trim(), ignoreCase = true).let {
                    if (!it) return false
                }
            }

            // Third: Ensuring that a file is not added already
            existingPaths.none { existingPath ->
                existingPath.equals(path, ignoreCase = true)
            }.let { if (!it) return false }

            return true
        }

        fun CachedFile.getSelectableFilesFromStorage(): List<SelectableFile> {
            val selectableFiles = mutableListOf<SelectableFile>()

            walk { file ->
                if (!file.isValid()) return@walk

                selectableFiles.add(
                    SelectableFile(
                        data = file,
                        selected = false
                    )
                )
            }

            return selectableFiles
        }

        fun getAllStorages(): List<CachedFile> {
            return application.contentResolver.persistedUriPermissions.mapNotNull { permission ->
                val storage = CachedFileCompat.fromUri(
                    application,
                    permission.uri,
                    builder = CachedFileCompat.build(
                        name = UUID.randomUUID().toString(),
                        size = 0,
                        lastModified = 0
                    )
                )
                if (!storage.isDirectory) return@mapNotNull null

                storage
            }.let { storages ->
                storages.filter { storage ->
                    storages.none {
                        it.path != storage.path && storage.path.startsWith(
                            it.path,
                            ignoreCase = true
                        )
                    }
                }
            }
        }

        return try {
            val storages = getAllStorages()
            val files = mutableListOf<SelectableFile>()

            for (storage in storages) {
                files.addAll(storage.getSelectableFilesFromStorage())
            }

            Log.i(GET_FILES, "Successfully got all matching files.")
            files
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(GET_FILES, "Couldn't get all matching files.")

            emptyList()
        }
    }

    /**
     * Gets book from given file. If error happened, returns [NullableBook.Null].
     */
    override suspend fun getBookFromFile(cachedFile: CachedFile): NullableBook {
        val parsedBook = fileParser.parse(cachedFile)
        if (parsedBook == null) {
            Log.e(GET_BOOK_FROM_FILE, "Parsed file(${cachedFile.name}) is null.")
            return Null(
                cachedFile.name,
                UIText.StringResource(R.string.error_something_went_wrong)
            )
        }

        Log.i(GET_BOOK_FROM_FILE, "Successfully got book from file.")
        return NotNull(bookWithCover = parsedBook)
    }

    override suspend fun copyExternalBookToPrivateStorage(uri: Uri): CachedFile? {
        val fileName = resolveFileName(uri)
        val mimeType = application.contentResolver.getType(uri)
        val extension = resolveSupportedExtension(fileName, mimeType)

        if (extension == null) {
            Log.w(IMPORT_BOOK, "Unsupported external book: $uri, mimeType: $mimeType.")
            return null
        }

        val importsDir = File(application.filesDir, "imports")
        if (!importsDir.exists()) {
            importsDir.mkdirs()
        }

        val targetFile = resolveTargetFile(
            importsDir = importsDir,
            fileName = fileName,
            extension = extension
        )
        val tempFile = File(importsDir, "${targetFile.name}.tmp")

        return try {
            application.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Failed to open external book stream.")

            if (!tempFile.renameTo(targetFile)) {
                throw IllegalStateException("Failed to rename imported book.")
            }

            Log.i(IMPORT_BOOK, "Copied external book to ${targetFile.absolutePath}.")
            CachedFileCompat.fromUri(
                context = application,
                uri = Uri.fromFile(targetFile),
                builder = CachedFileCompat.build(
                    name = targetFile.name,
                    path = targetFile.absolutePath,
                    size = targetFile.length(),
                    lastModified = targetFile.lastModified(),
                    isDirectory = false
                )
            )
        } catch (e: Exception) {
            tempFile.delete()
            targetFile.delete()
            e.printStackTrace()
            Log.e(IMPORT_BOOK, "Could not import external book.")
            null
        }
    }

    private fun resolveFileName(uri: Uri): String {
        if (uri.scheme == "file") {
            return File(uri.path.orEmpty()).name
        }

        application.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val displayName = cursor.getString(index)
                    if (!displayName.isNullOrBlank()) return displayName
                }
            }
        }

        return uri.lastPathSegment?.substringAfterLast('/') ?: "book"
    }

    private fun resolveSupportedExtension(fileName: String, mimeType: String?): String? {
        val extensionFromName = fileName
            .substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.let { ".$it".lowercase() }

        if (extensionFromName in externalImportExtensions) return extensionFromName

        val normalizedMimeType = mimeType?.lowercase()
        return mimeTypeExtensions[normalizedMimeType]
            ?: normalizedMimeType
                ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
                ?.let { ".$it".lowercase() }
                ?.takeIf { it in externalImportExtensions }
    }

    private fun resolveTargetFile(
        importsDir: File,
        fileName: String,
        extension: String
    ): File {
        val rawName = fileName.substringBeforeLast('.', missingDelimiterValue = fileName)
        val sanitizedName = rawName
            .replace(Regex("[^A-Za-z0-9._ -]"), "_")
            .trim('.', ' ', '_', '-')
            .ifBlank { "book" }
        val targetFile = File(importsDir, "$sanitizedName$extension")

        if (!targetFile.exists()) return targetFile

        return File(importsDir, "${sanitizedName}_${UUID.randomUUID()}$extension")
    }

    private companion object {
        val externalImportExtensions = provideExtensions().toSet()
        val mimeTypeExtensions = mapOf(
            "application/epub+zip" to ".epub",
            "application/pdf" to ".pdf",
            "application/x-fictionbook+xml" to ".fb2",
            "application/fb2+xml" to ".fb2",
            "text/plain" to ".txt",
            "text/html" to ".html",
            "text/markdown" to ".md",
            "text/x-markdown" to ".md"
        )
    }
}
