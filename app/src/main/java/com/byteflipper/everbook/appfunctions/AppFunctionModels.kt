/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.appfunctions

import android.app.PendingIntent
import androidx.appfunctions.AppFunctionSerializable

/**
 * A book stored in the user's EverBook library.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class LibraryBook(
    /** Stable identifier of the book inside the library. Pass it to openBook. */
    val id: Int,
    /** Title of the book. */
    val title: String,
    /** Author of the book, or null when the file provided no author. */
    val author: String? = null,
    /** Reading progress of the book, from 0 to 100 percent. */
    val progressPercent: Int = 0,
    /** True when the book has been read to the end. */
    val isFinished: Boolean = false,
    /**
     * Time the book was opened for the last time, in milliseconds since the Unix epoch.
     * Zero when the book has never been opened.
     */
    val lastOpenedTimestampMillis: Long = 0
)

/**
 * The book to read and the intent that opens it.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class OpenBookResponse(
    /** The book that the returned intent opens. */
    val book: LibraryBook,
    /**
     * Intent that opens EverBook on this book at the last saved reading position.
     * Send it to bring the user into the reader; nothing is shown until it is sent.
     */
    val openBookIntent: PendingIntent
)

/**
 * Where the user currently is in a book.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CurrentReadingContext(
    /** The book being read. */
    val book: LibraryBook,
    /** Title of the chapter at the reading position, or null when the book has no chapters. */
    val chapterTitle: String? = null,
    /** How far into the current chapter the user is, from 0 to 100 percent. */
    val chapterProgressPercent: Int = 0,
    /**
     * Short piece of the book text at the reading position, at most 600 characters. Null when the
     * reader is closed or the book renders as a native PDF, since the text is not loaded then.
     */
    val excerpt: String? = null,
    /**
     * True when the reader is open right now, so the position is live. False when it comes from the
     * last saved position of the book the user read most recently.
     */
    val isReaderOpen: Boolean = false
)

/**
 * Summary of the user's reading activity across the whole library.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ReadingStatsSummary(
    /** Total time spent reading, in minutes, over all recorded sessions. */
    val totalReadingMinutes: Long = 0,
    /** Number of consecutive days up to today with at least one reading session. */
    val currentStreakDays: Int = 0,
    /** Longest daily reading streak ever recorded, in days. */
    val longestStreakDays: Int = 0,
    /** Average reading time, in minutes, on days the user actually read. */
    val averageMinutesPerActiveDay: Long = 0,
    /** Number of books read to the end. */
    val booksFinished: Int = 0,
    /** Number of books that are started but not finished yet. */
    val booksInProgress: Int = 0
)
