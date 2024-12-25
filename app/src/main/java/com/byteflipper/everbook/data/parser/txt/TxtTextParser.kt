package com.byteflipper.everbook.data.parser.txt

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import com.byteflipper.everbook.R
import com.byteflipper.everbook.data.parser.TextParser
import com.byteflipper.everbook.domain.model.Chapter
import com.byteflipper.everbook.domain.model.ChapterWithText
import com.byteflipper.everbook.domain.util.Resource
import com.byteflipper.everbook.domain.util.UIText
import com.byteflipper.everbook.presentation.core.util.clearAllMarkdown
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

private const val TXT_TAG = "TXT Parser"

class TxtTextParser @Inject constructor() : TextParser {

    override suspend fun parse(file: File): Resource<List<ChapterWithText>> {
        Log.i(TXT_TAG, "Started TXT parsing: ${file.name}.")

        return try {
            val lines = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                BufferedReader(FileReader(file)).forEachLine { line ->
                    if (line.isNotBlank()) {
                        lines.add(
                            line.trim()
                        )
                    }
                }
            }

            yield()

            if (lines.size < 2) {
                return Resource.Error(UIText.StringResource(R.string.error_file_empty))
            }

            val title = lines.first().clearAllMarkdown().let { title ->
                lines.removeAt(0)
                if (title.isBlank()) return@let "Chapter 1"
                return@let title
            }

            Log.i(TXT_TAG, "Successfully finished TXT parsing.")
            Resource.Success(
                listOf(
                    ChapterWithText(
                        chapter = Chapter(
                            index = 0,
                            title = title,
                            startIndex = 0,
                            endIndex = lines.lastIndex
                        ),
                        text = lines
                    )
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(
                UIText.StringResource(
                    R.string.error_query,
                    e.message?.take(40)?.trim() ?: ""
                )
            )
        }
    }
}