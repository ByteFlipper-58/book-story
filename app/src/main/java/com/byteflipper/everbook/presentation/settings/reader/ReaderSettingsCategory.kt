@file:Suppress("FunctionName")

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.byteflipper.everbook.presentation.settings.reader.chapters.ChaptersSubcategory
import com.byteflipper.everbook.presentation.settings.reader.font.FontSubcategory
import com.byteflipper.everbook.presentation.settings.reader.images.ImagesSubcategory
import com.byteflipper.everbook.presentation.settings.reader.misc.MiscSubcategory
import com.byteflipper.everbook.presentation.settings.reader.padding.PaddingSubcategory
import com.byteflipper.everbook.presentation.settings.reader.progress.ProgressSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_mode.ReadingModeSubcategory
import com.byteflipper.everbook.presentation.settings.reader.reading_speed.ReadingSpeedSubcategory
import com.byteflipper.everbook.presentation.settings.reader.system.SystemSubcategory
import com.byteflipper.everbook.presentation.settings.reader.text.TextSubcategory
import com.byteflipper.everbook.presentation.settings.reader.translator.TranslatorSubcategory

fun LazyListScope.ReaderSettingsCategory(
    titleColor: @Composable () -> Color = { MaterialTheme.colorScheme.primary }
) {
    FontSubcategory(
        titleColor = titleColor
    )
    TextSubcategory(
        titleColor = titleColor
    )
    ImagesSubcategory(
        titleColor = titleColor
    )
    ChaptersSubcategory(
        titleColor = titleColor
    )
    ReadingModeSubcategory(
        titleColor = titleColor
    )
    PaddingSubcategory(
        titleColor = titleColor
    )
    SystemSubcategory(
        titleColor = titleColor
    )
    ReadingSpeedSubcategory(
        titleColor = titleColor
    )
    ProgressSubcategory(
        titleColor = titleColor
    )
    TranslatorSubcategory(
        titleColor = titleColor
    )
    MiscSubcategory(
        titleColor = titleColor,
        showDivider = false
    )
}