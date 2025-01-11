@file:Suppress("FunctionName")

package com.byteflipper.everbook.presentation.settings.browse

import androidx.compose.foundation.lazy.LazyListScope
import com.byteflipper.everbook.presentation.settings.browse.filter.BrowseFilterSubcategory
import com.byteflipper.everbook.presentation.settings.browse.general.BrowseGeneralSubcategory
import com.byteflipper.everbook.presentation.settings.browse.sort.BrowseSortSubcategory

fun LazyListScope.BrowseSettingsCategory() {
    BrowseGeneralSubcategory()
    BrowseFilterSubcategory()
    BrowseSortSubcategory(
        showDivider = false
    )
}