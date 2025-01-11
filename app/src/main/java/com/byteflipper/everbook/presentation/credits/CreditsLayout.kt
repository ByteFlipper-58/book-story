package com.byteflipper.everbook.presentation.credits

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideCredits
import com.byteflipper.everbook.ui.about.AboutEvent

@Composable
fun CreditsLayout(
    paddingValues: PaddingValues,
    listState: LazyListState,
    navigateToBrowserPage: (AboutEvent.OnNavigateToBrowserPage) -> Unit
) {
    val context = LocalContext.current
    LazyColumnWithScrollbar(
        Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        state = listState
    ) {
        items(
            Constants.provideCredits(),
            key = { it.name }
        ) { credit ->
            CreditItem(credit = credit) {
                credit.website?.let { website ->
                    navigateToBrowserPage(
                        AboutEvent.OnNavigateToBrowserPage(
                            page = website,
                            context = context
                        )
                    )
                }
            }
        }
    }
}