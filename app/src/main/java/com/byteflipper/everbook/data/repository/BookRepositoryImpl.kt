/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.repository

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.byteflipper.everbook.data.local.room.BookDao
import com.byteflipper.everbook.data.local.room.BookCategoryDao
import com.byteflipper.everbook.data.mapper.book.BookMapper
import com.byteflipper.everbook.data.parser.FileParser
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.file.CachedFileCompat
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.library.book.BookWithCover
import com.byteflipper.everbook.domain.reader.ReaderText
import com.byteflipper.everbook.domain.repository.BookRepository
import com.byteflipper.everbook.domain.util.CoverImage
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_TEXT = "GET TEXT, REPO"
private const val GET_BOOKS = "GET BOOKS, REPO"
private const val GET_BOOKS_BY_ID = "GET BOOKS, REPO"
private const val INSERT_BOOK = "INSERT BOOK, REPO"
private const val UPDATE_BOOK = "UPDATE BOOK, REPO"
private const val DELETE_BOOKS = "DELETE BOOKS, REPO"
private const val CAN_RESET_COVER = "CAN RESET COVER, REPO"
private const val RESET_COVER = "RESET COVER, REPO"

@Suppress("DEPRECATION")
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val application: Application,
    private val database: BookDao,
    private val bookCategoryDao: BookCategoryDao,

    private val bookMapper: BookMapper,

    private val fileParser: FileParser,
    private val textParser: TextParser
) : BookRepository {

    /**
     * Get all books matching [query] from database.
     * Empty [query] equals to all books.
     */
    override suspend fun getBooks(query: String): List<Book> {
        Log.i(GET_BOOKS, "Searching for books with query: \"$query\".")
        val entities = database.searchBooks(query)

        Log.i(GET_BOOKS, "Found ${entities.size} books.")
        return entities.map { entity ->
            val book = bookMapper.toBook(entity)

            // Получаем принадлежность книги к категориям many-to-many
            val categories = bookCategoryDao.getCategoriesForBook(book.id)

            val lastHistory = database.getLatestHistoryForBook(book.id)

            book.copy(
                lastOpened = lastHistory?.time,
                categoryIds = categories
            )
        }
    }

    /**
     * Get all books that match given [ids].
     */
    override suspend fun getBooksById(ids: List<Int>): List<Book> {
        Log.i(GET_BOOKS_BY_ID, "Getting books with ids: $ids.")
        val entities = database.findBooksById(ids)

        return entities.map { entity ->
            val book = bookMapper.toBook(entity)

            val categories = bookCategoryDao.getCategoriesForBook(book.id)

            val lastHistory = database.getLatestHistoryForBook(book.id)

            book.copy(
                lastOpened = lastHistory?.time,
                categoryIds = categories
            )
        }
    }

    /**
     * Loads text from the book. Already formatted.
     */
    override suspend fun getBookText(bookId: Int): List<ReaderText> {
        if (bookId == -1) return emptyList()

        val book = database.findBookById(bookId)
        val cachedFile = CachedFileCompat.fromFullPath(
            context = application,
            path = book.filePath,
            builder = CachedFileCompat.build(
                name = book.filePath.substringAfterLast(File.separator),
                path = book.filePath,
                isDirectory = false
            )
        )

        if (cachedFile == null || !cachedFile.canAccess()) {
            Log.e(GET_TEXT, "File [$bookId] does not exist")
            return emptyList()
        }

        val readerText = textParser.parse(cachedFile)

        if (
            readerText.filterIsInstance<ReaderText.Text>().isEmpty() ||
            readerText.filterIsInstance<ReaderText.Chapter>().isEmpty()
        ) {
            Log.e(GET_TEXT, "Could not load text from [$bookId].")
            return emptyList()
        }

        Log.i(GET_TEXT, "Successfully loaded text of [$bookId] with markdown.")
        return readerText
    }

    /**
     * Inserts book in database.
     * Creates covers folder, which contains cover.
     */
    override suspend fun insertBook(
        bookWithCover: BookWithCover
    ) {
        Log.i(INSERT_BOOK, "Inserting ${bookWithCover.book.title}.")

        val filesDir = application.filesDir
        val coversDir = File(filesDir, "covers")

        if (!coversDir.exists()) {
            Log.i(INSERT_BOOK, "Created covers folder.")
            coversDir.mkdirs()
        }

        var coverUri = ""

        if (bookWithCover.coverImage != null) {
            try {
                coverUri = "${UUID.randomUUID()}.webp"
                val cover = File(coversDir, coverUri)

                withContext(Dispatchers.IO) {
                    BufferedOutputStream(FileOutputStream(cover)).use { output ->
                        bookWithCover.coverImage
                            .copy(Bitmap.Config.RGB_565, false)
                            .compress(Bitmap.CompressFormat.WEBP, 20, output)
                            .let { success ->
                                if (success) return@let
                                throw Exception("Couldn't save cover image")
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(INSERT_BOOK, "Could not save cover.")
                coverUri = ""
                e.printStackTrace()
            }
        }

        val updatedBook = bookWithCover.book.copy(
            coverImage = if (coverUri.isNotBlank()) {
                Uri.fromFile(File("$coversDir/$coverUri"))
            } else null
        )

        val bookToInsert = bookMapper.toBookEntity(updatedBook)
        val generatedId = database.insertBook(bookToInsert).toInt()

        val refs = mutableListOf<com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef>()
        refs.add(com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(generatedId, 0))
        if (updatedBook.categoryId != 0) {
            refs.add(com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(generatedId, updatedBook.categoryId))
        }
        bookCategoryDao.insertAll(refs)
        Log.i(INSERT_BOOK, "Successfully inserted book.")
    }

    /**
     * Update book without cover image.
     */
    override suspend fun updateBook(book: Book) {
        val entity = database.findBookById(book.id)
        database.updateBooks(
            listOf(
                bookMapper.toBookEntity(
                    book.copy(
                        coverImage = if (entity.image != null) entity.image.toUri() else null
                    )
                )
            )
        )

        // Обновляем связи категорий, если параметр categoryId изменён (временно однокатегорийная модель)
        // Для будущего перехода на plural категории используется setCategories().
        bookCategoryDao.deleteByBook(book.id)
        val refs = mutableListOf<com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef>()
        refs.add(com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(book.id, 0))
        if (book.categoryId != 0) refs.add(com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(book.id, book.categoryId))
        bookCategoryDao.insertAll(refs)
    }

    /**
     * Update cover image of the book. Deletes old cover and replaces with new.
     */
    override suspend fun updateCoverImageOfBook(
        bookWithOldCover: Book,
        newCoverImage: CoverImage?
    ) {
        Log.i(UPDATE_BOOK, "Updating cover image: ${bookWithOldCover.title}.")

        val book = database.findBookById(bookWithOldCover.id)
        var uri: String? = null

        val filesDir = application.filesDir
        val coversDir = File(filesDir, "covers")

        if (!coversDir.exists()) {
            Log.i(UPDATE_BOOK, "Created covers folder.")
            coversDir.mkdirs()
        }

        if (newCoverImage != null) {
            try {
                uri = "${UUID.randomUUID()}.webp"
                val cover = File(coversDir, uri)

                withContext(Dispatchers.IO) {
                    BufferedOutputStream(FileOutputStream(cover)).use { output ->
                        newCoverImage
                            .copy(Bitmap.Config.RGB_565, false)
                            .compress(Bitmap.CompressFormat.WEBP, 20, output)
                            .let { success ->
                                if (success) return@let
                                throw Exception("Couldn't save cover image")
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e(UPDATE_BOOK, "Could not save new cover.")
                e.printStackTrace()
                return
            }
        }

        if (book.image != null) {
            try {
                val fileToDelete = File(
                    "$coversDir${File.separator}${book.image.substringAfterLast(File.separator)}"
                )

                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            } catch (e: Exception) {
                Log.e(UPDATE_BOOK, "Could not delete old cover.")
                e.printStackTrace()
            }
        }

        val newCoverImageUri = if (uri != null) {
            Uri.fromFile(File("$coversDir/$uri"))
        } else {
            null
        }

        val bookWithNewCover = bookWithOldCover.copy(
            coverImage = newCoverImageUri
        )

        database.updateBooks(
            listOf(
                bookMapper.toBookEntity(
                    bookWithNewCover
                )
            )
        )
        Log.i(UPDATE_BOOK, "Successfully updated cover image.")
    }

    /**
     * Delete books.
     * Also deletes cover image and text from internal storage.
     */
    override suspend fun deleteBooks(books: List<Book>) {
        Log.i(DELETE_BOOKS, "Deleting books.")

        val filesDir = application.filesDir
        val coversDir = File(filesDir, "covers")

        if (!coversDir.exists()) {
            Log.i(DELETE_BOOKS, "Created covers folder.")
            coversDir.mkdirs()
        }

        for (b in books) {
            bookCategoryDao.deleteByBook(b.id)

            if (b.coverImage != null) {
                try {
                    val fileToDelete = File(
                        "$coversDir${File.separator}${b.coverImage?.path?.substringAfterLast(File.separator)}"
                    )

                    if (fileToDelete.exists()) {
                        fileToDelete.delete()
                    }
                } catch (e: Exception) {
                    Log.e(DELETE_BOOKS, "Could not delete cover image.")
                    e.printStackTrace()
                }
            }
        }

        database.deleteBooks(
            books.map {
                val book = database.findBookById(it.id)
                book
            }
        )

        Log.i(DELETE_BOOKS, "Successfully deleted books.")
    }

    /**
     * @return Whether can reset cover image (restore default).
     */
    override suspend fun canResetCover(bookId: Int): Boolean {
        val book = database.findBookById(bookId)

        val cachedFile = CachedFileCompat.fromFullPath(
            application,
            book.filePath,
            builder = CachedFileCompat.build(
                name = book.filePath.substringAfterLast(File.separator),
                path = book.filePath,
                isDirectory = false
            )
        )

        if (cachedFile == null || !cachedFile.canAccess()) {
            return false
        }

        val defaultCoverUncompressed = fileParser.parse(cachedFile)?.coverImage
            ?: return false

        if (book.image == null) {
            Log.i(CAN_RESET_COVER, "Can reset cover image. (current is null)")
            return true
        }

        val stream = ByteArrayOutputStream()
        defaultCoverUncompressed.copy(Bitmap.Config.RGB_565, false).compress(
            Bitmap.CompressFormat.WEBP,
            20,
            stream
        )
        val byteArray = stream.toByteArray()
        val defaultCover = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        val currentCover = try {
            MediaStore.Images.Media.getBitmap(
                application.contentResolver,
                book.image.toUri()
            )
        } catch (e: Exception) {
            Log.i(CAN_RESET_COVER, "Can reset cover image. (could not get current)")
            e.printStackTrace()
            return true
        }

        return !defaultCover.sameAs(currentCover)
    }

    /**
     * Reset cover image to default.
     * If there is no default cover, returns false.
     */
    override suspend fun resetCoverImage(bookId: Int): Boolean {
        if (!canResetCover(bookId)) {
            Log.w(RESET_COVER, "Cannot reset cover image.")
            return false
        }

        val book = database.findBookById(bookId)
        val cachedFile = CachedFileCompat.fromFullPath(
            application,
            book.filePath,
            builder = CachedFileCompat.build(
                name = book.filePath.substringAfterLast(File.separator),
                path = book.filePath,
                isDirectory = false
            )
        )

        if (cachedFile == null || !cachedFile.canAccess()) {
            return false
        }

        val defaultCover = fileParser.parse(cachedFile)?.coverImage
            ?: return false
        updateCoverImageOfBook(bookMapper.toBook(book), defaultCover)

        Log.i(RESET_COVER, "Successfully reset cover image.")
        return true
    }


    override suspend fun setCategories(bookId: Int, categoryIds: List<Int>) {
        val finalIds = (categoryIds + 0).distinct()

        bookCategoryDao.deleteByBook(bookId)
        val refs = finalIds.map { cid -> com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(bookId, cid) }
        bookCategoryDao.insertAll(refs)

       val primaryCategoryId = finalIds.firstOrNull { it != 0 } ?: 0
        val entity = database.findBookById(bookId).copy(categoryId = primaryCategoryId)
        database.updateBooks(listOf(entity))
    }

    override suspend fun addBookToCategory(bookId: Int, categoryId: Int) {
        if (categoryId == 0) return // already ensured
        bookCategoryDao.insertAll(
            listOf(
                com.byteflipper.everbook.data.local.dto.BookCategoryCrossRef(bookId, categoryId)
            )
        )

        val entity = database.findBookById(bookId)
        if (entity.categoryId == 0) {
            database.updateBooks(listOf(entity.copy(categoryId = categoryId)))
        }
    }

    override suspend fun removeBookFromCategory(bookId: Int, categoryId: Int) {
        if (categoryId == 0) return
        bookCategoryDao.delete(bookId, categoryId)

        val entity = database.findBookById(bookId)
        if (entity.categoryId == categoryId) {
            database.updateBooks(listOf(entity.copy(categoryId = 0)))
        }
    }
}