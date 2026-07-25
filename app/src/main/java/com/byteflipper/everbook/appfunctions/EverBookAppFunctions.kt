/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.appfunctions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.service.AppFunction
import com.byteflipper.everbook.domain.assist.ReadingContextTracker
import com.byteflipper.everbook.domain.library.book.Book
import com.byteflipper.everbook.domain.use_case.book.GetBookById
import com.byteflipper.everbook.domain.use_case.data_store.GetDatastore
import com.byteflipper.everbook.domain.use_case.book.GetBooks
import com.byteflipper.everbook.domain.use_case.history.GetLatestAvailableHistoryBook
import com.byteflipper.everbook.domain.use_case.statistics.GetReadingStatistics
import com.byteflipper.everbook.presentation.core.constants.DataStoreConstants
import com.byteflipper.everbook.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * EverBook's reading workflows, exposed to on-device AI agents through the AppFunctions API.
 *
 * Only read-only library queries plus an intent that opens the reader are exposed: none of these
 * functions modify or delete user data. Instances are provided to the AppFunctions service by
 * com.byteflipper.everbook.Application, which owns the Hilt graph.
 */
@Singleton
class EverBookAppFunctions @Inject constructor(
    private val getBooks: GetBooks,
    private val getBookById: GetBookById,
    private val getLatestAvailableHistoryBook: GetLatestAvailableHistoryBook,
    private val getReadingStatistics: GetReadingStatistics,
    private val readingContextTracker: ReadingContextTracker,
    private val getDatastore: GetDatastore
) {

    /**
     * Lists the books in the user's EverBook library, most recently opened first.
     *
     * Call this first to resolve a book the user refers to by name, then pass the id of the
     * returned book to openBook.
     *
     * @param appFunctionContext The execution context.
     * @param query Case insensitive part of the book title. Omit it to list the whole library.
     * @param limit Maximum number of books to return, from 1 to 50.
     * @return The matching books, or an empty list when the library holds no such book.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchBooks(
        appFunctionContext: AppFunctionContext,
        query: String? = null,
        limit: Int = 10
    ): List<LibraryBook> = withContext(Dispatchers.IO) {
        if (limit < 1 || limit > MAX_RESULTS) {
            throw AppFunctionInvalidArgumentException(
                "limit must be between 1 and $MAX_RESULTS, was $limit"
            )
        }

        getBooks.execute(query?.trim().orEmpty())
            .sortedByDescending { it.lastOpened ?: 0 }
            .take(limit)
            .map { it.toLibraryBook() }
    }

    /**
     * Returns the book the user read most recently, which is the one to continue with.
     *
     * @param appFunctionContext The execution context.
     * @return The most recently opened book that is still in the library, or null when the user
     * has not read anything yet.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getContinueReadingBook(
        appFunctionContext: AppFunctionContext
    ): LibraryBook? = withContext(Dispatchers.IO) {
        val bookId = getLatestAvailableHistoryBook.execute() ?: return@withContext null
        getBookById.execute(bookId)?.toLibraryBook()
    }

    /**
     * Prepares opening a book in the EverBook reader at the position the user stopped at.
     *
     * Resolve the id with searchBooks or getContinueReadingBook first. The reader is only shown
     * once the returned openBookIntent is sent.
     *
     * @param appFunctionContext The execution context.
     * @param bookId Identifier of the book to open, as returned by searchBooks.
     * @return The book together with the intent that opens it.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun openBook(
        appFunctionContext: AppFunctionContext,
        bookId: Int
    ): OpenBookResponse = withContext(Dispatchers.IO) {
        val book = getBookById.execute(bookId)
            ?: throw AppFunctionElementNotFoundException("No book found for id $bookId")

        OpenBookResponse(
            book = book.toLibraryBook(),
            openBookIntent = openBookPendingIntent(appFunctionContext.context, book.id)
        )
    }

    /**
     * Reports what the user is reading at the moment and where they are in the book.
     *
     * Use this for questions about the book at hand: what is being read, which chapter, what the
     * text says at that point. While the reader is open the position is live and a short excerpt of
     * the text is included; otherwise the last saved position of the most recent book is returned
     * without an excerpt.
     *
     * @param appFunctionContext The execution context.
     * @return The current reading position, or null when the user has not read anything yet or has
     * turned sharing of the reading context off in the app settings.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getCurrentReadingContext(
        appFunctionContext: AppFunctionContext
    ): CurrentReadingContext? = withContext(Dispatchers.IO) {
        val sharingAllowed = getDatastore
            .execute(DataStoreConstants.ASSISTANT_READING_CONTEXT) ?: true
        if (!sharingAllowed) return@withContext null

        readingContextTracker.current.value?.let { reading ->
            val book = getBookById.execute(reading.bookId)

            return@withContext CurrentReadingContext(
                book = book?.toLibraryBook() ?: LibraryBook(
                    id = reading.bookId,
                    title = reading.title,
                    author = reading.author,
                    progressPercent = reading.progress.toPercent(),
                    isFinished = reading.progress >= 1f
                ),
                chapterTitle = reading.chapterTitle,
                chapterProgressPercent = reading.chapterProgress.toPercent(),
                excerpt = reading.excerpt,
                isReaderOpen = true
            )
        }

        // Reader closed: fall back to the saved position. No excerpt — pulling one would mean
        // parsing the whole book inside the AppFunctions service.
        val bookId = getLatestAvailableHistoryBook.execute() ?: return@withContext null
        val book = getBookById.execute(bookId) ?: return@withContext null

        CurrentReadingContext(
            book = book.toLibraryBook(),
            isReaderOpen = false
        )
    }

    /**
     * Reports how much the user has been reading, aggregated over the whole library.
     *
     * @param appFunctionContext The execution context.
     * @return Total reading time, reading streaks and how many books are finished or in progress.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getReadingStatsSummary(
        appFunctionContext: AppFunctionContext
    ): ReadingStatsSummary = withContext(Dispatchers.IO) {
        val statistics = getReadingStatistics.execute()

        ReadingStatsSummary(
            totalReadingMinutes = statistics.totalTimeMs.inWholeMinutes(),
            currentStreakDays = statistics.streakDays,
            longestStreakDays = statistics.longestStreakDays,
            averageMinutesPerActiveDay = statistics.averagePerActiveDayMs.inWholeMinutes(),
            booksFinished = statistics.booksFinished,
            booksInProgress = statistics.booksInProgress
        )
    }

    private fun openBookPendingIntent(context: Context, bookId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_BOOK
            putExtra(MainActivity.EXTRA_BOOK_ID, bookId)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        return PendingIntent.getActivity(
            context,
            bookId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun Book.toLibraryBook() = LibraryBook(
        id = id,
        title = title,
        author = author.getAsString(),
        progressPercent = progress.toPercent(),
        isFinished = progress >= 1f,
        lastOpenedTimestampMillis = lastOpened ?: 0
    )

    private fun Float.toPercent() = (this * 100).roundToInt().coerceIn(0, 100)

    private fun Long.inWholeMinutes() = this / 60_000L

    private companion object {
        const val MAX_RESULTS = 50
    }
}
