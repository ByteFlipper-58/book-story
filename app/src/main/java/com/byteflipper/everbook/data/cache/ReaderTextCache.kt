/*
 * EverBook - a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.cache

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import com.byteflipper.everbook.data.di.ApplicationScope
import com.byteflipper.everbook.data.parser.MarkdownParser
import com.byteflipper.everbook.data.parser.ReaderTextChunkBuffer
import com.byteflipper.everbook.data.parser.ReaderTextChunkSink
import com.byteflipper.everbook.domain.file.CachedFile
import com.byteflipper.everbook.domain.reader.ReaderText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val READER_TEXT_CACHE = "ReaderTextCache"
private const val CACHE_SCHEMA_VERSION = 1
private const val PARSER_VERSION = 1
private const val CACHE_ENTRY_CHUNK_SIZE = 512

private const val TYPE_CHAPTER = "chapter"
private const val TYPE_TEXT = "text"
private const val TYPE_SEPARATOR = "separator"
private const val TYPE_IMAGE = "image"
private const val TYPE_MATH = "math"

@Singleton
class ReaderTextCache @Inject constructor(
    private val application: Application,
    private val markdownParser: MarkdownParser,
    @ApplicationScope
    private val applicationScope: CoroutineScope
) {
    private val gson = Gson()
    private val warmupJobs = ConcurrentHashMap<Int, Job>()
    private val rootDir: File
        get() = File(application.filesDir, "reader_text_cache")

    suspend fun read(
        bookId: Int,
        cachedFile: CachedFile,
        onChunk: ReaderTextChunkSink? = null
    ): List<ReaderText>? {
        return withContext(Dispatchers.IO) {
            try {
                val bookDir = bookDir(bookId)
                val manifestFile = File(bookDir, MANIFEST_FILE_NAME)
                if (!manifestFile.exists()) return@withContext null

                val manifest = manifestFile.bufferedReader().use { reader ->
                    gson.fromJson(reader, ReaderTextCacheManifest::class.java)
                } ?: return@withContext null

                if (!manifest.matches(cachedFile)) {
                    Log.i(READER_TEXT_CACHE, "Cache miss for [$bookId].")
                    return@withContext null
                }

                val chunkBuffer = ReaderTextChunkBuffer(onChunk = onChunk)
                val readerText = ArrayList<ReaderText>()
                val chunkFiles = manifest.chunkFiles?.takeIf { it.isNotEmpty() }
                    ?: return@withContext null

                for (chunkFile in chunkFiles) {
                    val entries = readEntryChunk(bookDir, chunkFile)
                        ?: return@withContext null
                    for (entry in entries) {
                        val text = entry.toReaderText(bookDir) ?: return@withContext null
                        readerText.add(text)
                        chunkBuffer.add(text)
                    }
                }
                chunkBuffer.flush()

                if (readerText.isEmpty()) return@withContext null

                Log.i(READER_TEXT_CACHE, "Cache hit for [$bookId].")
                readerText
            } catch (e: Exception) {
                Log.w(READER_TEXT_CACHE, "Could not read cache for [$bookId].", e)
                null
            }
        }
    }

    suspend fun write(bookId: Int, cachedFile: CachedFile, readerText: List<ReaderText>) {
        withContext(Dispatchers.IO) {
            val tempDir = File(rootDir, "book_$bookId.tmp")
            val finalDir = bookDir(bookId)

            try {
                tempDir.deleteRecursively()
                tempDir.mkdirs()
                val chunksDir = File(tempDir, CHUNKS_DIR_NAME)
                chunksDir.mkdirs()

                var imageIndex = 0
                val entries = readerText.map { entry ->
                    entry.toCachedEntry(
                        bookDir = tempDir,
                        imageIndex = imageIndex
                    ).also {
                        if (entry is ReaderText.Image) imageIndex++
                    }
                }
                val chunkFiles = entries.chunked(CACHE_ENTRY_CHUNK_SIZE)
                    .mapIndexed { index, chunk ->
                        val chunkFileName = "${index.toString().padStart(6, '0')}.json"
                        File(chunksDir, chunkFileName).bufferedWriter().use { writer ->
                            gson.toJson(chunk, writer)
                        }
                        "$CHUNKS_DIR_NAME/$chunkFileName"
                    }

                val manifest = ReaderTextCacheManifest(
                    schemaVersion = CACHE_SCHEMA_VERSION,
                    parserVersion = PARSER_VERSION,
                    path = cachedFile.path,
                    size = cachedFile.size,
                    lastModified = cachedFile.lastModified,
                    chunkFiles = chunkFiles
                )

                File(tempDir, MANIFEST_FILE_NAME).bufferedWriter().use { writer ->
                    gson.toJson(manifest, writer)
                }

                finalDir.deleteRecursively()
                if (!tempDir.renameTo(finalDir)) {
                    throw IllegalStateException("Could not move cache into place.")
                }

                Log.i(READER_TEXT_CACHE, "Wrote cache for [$bookId].")
            } catch (e: Exception) {
                Log.w(READER_TEXT_CACHE, "Could not write cache for [$bookId].", e)
                tempDir.deleteRecursively()
            }
        }
    }

    fun warmUp(
        bookId: Int,
        cachedFile: CachedFile,
        readerTextProvider: suspend () -> List<ReaderText>
    ) {
        synchronized(warmupJobs) {
            warmupJobs[bookId]?.takeIf { !it.isCompleted }?.let {
                return
            }

            val job = applicationScope.launch {
                try {
                    if (read(bookId, cachedFile) != null) return@launch
                    val readerText = readerTextProvider()
                    if (readerText.isNotEmpty()) {
                        write(bookId, cachedFile, readerText)
                    }
                } finally {
                    warmupJobs.remove(bookId)
                }
            }

            warmupJobs[bookId] = job
        }
    }

    suspend fun delete(bookId: Int) {
        withContext(Dispatchers.IO) {
            try {
                warmupJobs.remove(bookId)?.cancel()
                bookDir(bookId).deleteRecursively()
                File(rootDir, "book_$bookId.tmp").deleteRecursively()
            } catch (e: Exception) {
                Log.w(READER_TEXT_CACHE, "Could not delete cache for [$bookId].", e)
            }
        }
    }

    fun cancelWarmUp(bookId: Int) {
        warmupJobs.remove(bookId)?.cancel()
    }

    fun cancelWarmUps() {
        synchronized(warmupJobs) {
            warmupJobs.values.forEach { job ->
                job.cancel()
            }
            warmupJobs.clear()
        }
    }

    private fun bookDir(bookId: Int): File {
        return File(rootDir, "book_$bookId")
    }

    private fun ReaderTextCacheManifest.matches(cachedFile: CachedFile): Boolean {
        return schemaVersion == CACHE_SCHEMA_VERSION &&
                parserVersion == PARSER_VERSION &&
                path == cachedFile.path &&
                size == cachedFile.size &&
                lastModified == cachedFile.lastModified
    }

    private fun readEntryChunk(
        bookDir: File,
        chunkFile: String
    ): List<ReaderTextCacheEntry>? {
        val file = File(bookDir, chunkFile)
            .takeIf { it.exists() && it.canRead() }
            ?: return null
        return file.bufferedReader().use { reader ->
            gson.fromJson(reader, Array<ReaderTextCacheEntry>::class.java)
        }?.toList()
    }

    private fun ReaderTextCacheEntry.toReaderText(bookDir: File): ReaderText? {
        return when (type) {
            TYPE_CHAPTER -> ReaderText.Chapter(
                title = text ?: return null,
                nested = nested ?: false
            )

            TYPE_TEXT -> {
                val source = text ?: return null
                ReaderText.Text(
                    line = markdownParser.parse(source),
                    source = source
                )
            }

            TYPE_SEPARATOR -> ReaderText.Separator

            TYPE_IMAGE -> {
                val fileName = imageFile ?: return null
                val imageFile = File(bookDir, fileName).takeIf { it.exists() && it.canRead() }
                    ?: return null

                ReaderText.Image(
                    imagePath = imageFile.path
                )
            }

            TYPE_MATH -> ReaderText.Math(text ?: return null)

            else -> null
        }
    }

    private fun ReaderText.toCachedEntry(bookDir: File, imageIndex: Int): ReaderTextCacheEntry {
        return when (this) {
            is ReaderText.Chapter -> ReaderTextCacheEntry(
                type = TYPE_CHAPTER,
                text = title,
                nested = nested
            )

            is ReaderText.Text -> ReaderTextCacheEntry(
                type = TYPE_TEXT,
                text = source
            )

            ReaderText.Separator -> ReaderTextCacheEntry(
                type = TYPE_SEPARATOR
            )

            is ReaderText.Image -> {
                val fileName = "image_$imageIndex.png"
                val imageFile = File(bookDir, fileName)

                val sourcePath = imagePath
                if (sourcePath != null) {
                    File(sourcePath).copyTo(imageFile, overwrite = true)
                } else {
                    val bitmap = imageBitmap ?: throw IllegalStateException("Image has no source.")
                    FileOutputStream(imageFile).use { output ->
                        val saved = bitmap.asAndroidBitmap()
                            .compress(Bitmap.CompressFormat.PNG, 100, output)
                        if (!saved) {
                            throw IllegalStateException("Could not save cached image.")
                        }
                    }
                }

                ReaderTextCacheEntry(
                    type = TYPE_IMAGE,
                    imageFile = fileName
                )
            }

            is ReaderText.Math -> ReaderTextCacheEntry(
                type = TYPE_MATH,
                text = latex
            )
        }
    }

    private companion object {
        const val MANIFEST_FILE_NAME = "manifest.json"
        const val CHUNKS_DIR_NAME = "chunks"
    }
}
