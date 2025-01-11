package com.byteflipper.everbook.data.parser

import com.byteflipper.everbook.domain.reader.ReaderText
import java.io.File

interface TextParser {
    suspend fun parse(file: File): List<ReaderText>
}