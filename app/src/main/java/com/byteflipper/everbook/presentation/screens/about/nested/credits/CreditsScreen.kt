package com.byteflipper.everbook.presentation.screens.about.nested.credits

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.byteflipper.everbook.R
import com.byteflipper.everbook.presentation.core.components.common.GoBackButton
import com.byteflipper.everbook.presentation.core.components.common.LazyColumnWithScrollbar
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.core.constants.Constants
import com.byteflipper.everbook.presentation.core.constants.provideCredits
import com.byteflipper.everbook.presentation.core.navigation.LocalNavigator
import com.byteflipper.everbook.presentation.core.util.showToast
import com.byteflipper.everbook.presentation.screens.about.data.AboutEvent
import com.byteflipper.everbook.presentation.screens.about.data.AboutViewModel
import com.byteflipper.everbook.presentation.screens.about.nested.credits.components.CreditItem

@Composable
fun CreditsScreenRoot() {
    CreditsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsScreen() {
    val onEvent = AboutViewModel.getEvent()
    val onNavigate = LocalNavigator.current
    val context = LocalContext.current

    val (scrollBehavior, lazyListState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

    Scaffold(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(id = R.string.credits_option))
                },
                navigationIcon = {
                    GoBackButton(onNavigate = onNavigate)
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumnWithScrollbar(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            state = lazyListState
        ) {
            items(Constants.provideCredits(), key = { it.name }) {
                CreditItem(credit = it) {
                    it.website?.let { website ->
                        onEvent(
                            AboutEvent.OnNavigateToBrowserPage(
                                page = website,
                                context = context,
                                noAppsFound = {
                                    context.getString(R.string.error_no_browser)
                                        .showToast(context = context, longToast = false)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}







