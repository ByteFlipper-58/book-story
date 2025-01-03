package com.byteflipper.everbook.data.repository

import android.os.Environment
import android.util.Log
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.data.mapper.book.BookMapper
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.model.BookWithTextAndCover
import com.byteflipper.everbook.domain.model.NullableBook
import com.byteflipper.everbook.domain.model.NullableBook.NotNull
import com.byteflipper.everbook.domain.model.NullableBook.Null
import com.byteflipper.everbook.domain.model.SelectableFile
import com.byteflipper.everbook.domain.repository.FileSystemRepository
import com.byteflipper.everbook.domain.util.Resource
import com.byteflipper.everbook.domain.util.UIText
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideSupportedExtensions
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_BOOK_FROM_FILE = "BOOK FROM FILE, REPO"
private const val GET_FILES_FROM_DEVICE = "FILES FROM DEVICE, REPO"

/**
 * File System repository.
 * Manages all File System([java.io.File]) related work.
 */
@Singleton
class FileSystemRepositoryImpl @Inject constructor(
    private val database: BookDao,

    private val bookMapper: BookMapper,

    private val fileParser: FileParser,
    private val textParser: TextParser,
) : FileSystemRepository {

    /**
     * Get all matching files from device.
     * Filters by [query] and sorts out not supported file formats and already added files.
     */
    override suspend fun getFilesFromDevice(query: String): List<SelectableFile> {
        Log.i(GET_FILES_FROM_DEVICE, "Getting files from device by query: \"$query\".")

        val existingBooks = database
            .searchBooks("")
            .map { bookMapper.toBook(it) }
        val supportedExtensions = Constants.provideSupportedExtensions()

        fun File.isValid(): Boolean {
            if (!exists()) {
                return false
            }

            val isFileSupported = supportedExtensions.any { ext ->
                name.endsWith(
                    ext,
                    ignoreCase = true
                )
            }

            if (!isFileSupported) {
                return false
            }

            val isFileNotAdded = existingBooks.all {
                it.filePath.lowercase().trim() != path.lowercase().trim()
            }

            if (!isFileNotAdded) {
                return false
            }

            val isQuery = if (query.isEmpty()) true else name.trim().lowercase()
                .contains(query.trim().lowercase())

            return isQuery
        }

        suspend fun File.getAllFiles(): List<SelectableFile> {
            val filesList = mutableListOf<SelectableFile>()

            val files = listFiles()
            if (files != null) {
                for (file in files) {
                    if (!file.exists()) {
                        continue
                    }

                    when {
                        file.isFile -> {
                            if (file.isValid()) {
                                filesList.add(
                                    SelectableFile(
                                        fileOrDirectory = file,
                                        parentDirectory = this,
                                        isDirectory = false,
                                        isFavorite = false,
                                        isSelected = false
                                    )
                                )
                            }
                        }

                        file.isDirectory -> {
                            val subDirectoryFiles = file.getAllFiles()
                            if (subDirectoryFiles.isNotEmpty()) {
                                filesList.add(
                                    SelectableFile(
                                        fileOrDirectory = file,
                                        parentDirectory = this,
                                        isDirectory = true,
                                        isFavorite = database.favoriteDirectoryExits(file.path),
                                        isSelected = false
                                    )
                                )
                                filesList.addAll(subDirectoryFiles)
                            }
                        }
                    }
                }
            }

            return filesList
        }

        val rootDirectory = Environment.getExternalStorageDirectory()
        if (
            !rootDirectory.exists() ||
            !rootDirectory.isDirectory ||
            (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED &&
                    Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)
        ) {
            Log.e(GET_FILES_FROM_DEVICE, "Could not correctly get root directory.")
            return emptyList()
        }

        Log.i(GET_FILES_FROM_DEVICE, "Successfully got all matching files.")
        return rootDirectory.getAllFiles()
    }

    /**
     * Gets book from given file. If error happened, returns [NullableBook.Null].
     */
    override suspend fun getBookFromFile(file: File): NullableBook {
        val parsedBook = fileParser.parse(file)
        if (parsedBook == null) {
            Log.e(GET_BOOK_FROM_FILE, "Parsed file(${file.name}) is null.")
            return Null(
                file.name,
                UIText.StringResource(R.string.error_something_went_wrong)
            )
        }

        val parsedText = textParser.parse(file)
        if (parsedText is Resource.Error) {
            Log.e(GET_BOOK_FROM_FILE, "Parsed text(${file.name}) has error.")
            return Null(
                file.name,
                parsedText.message
            )
        }

        Log.i(GET_BOOK_FROM_FILE, "Successfully got book from file.")
        return NotNull(
            bookWithTextAndCover = BookWithTextAndCover(
                book = parsedBook.book.copy(
                    chapters = parsedText.data!!.map { it.chapter }
                ),
                coverImage = parsedBook.coverImage,
                text = parsedText.data.map {
                    it.text
                }.flatten()
            )
        )
    }
}