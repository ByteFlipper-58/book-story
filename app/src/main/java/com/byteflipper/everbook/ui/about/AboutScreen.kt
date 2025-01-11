package com.byteflipper.everbook.ui.about

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.about.AboutContent
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.ui.credits.CreditsScreen
import com.byteflipper.everbook.ui.licenses.LicensesScreen

@Parcelize
object AboutScreen : Screen, Parcelable {

    @IgnoredOnParcel
    const val UPDATE_DIALOG = "update_dialog"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = hiltViewModel<AboutModel>()

        val state = screenModel.state.collectAsStateWithLifecycle()
        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior()

        AboutContent(
            scrollBehavior = scrollBehavior,
            dialog = state.value.dialog,
            updateLoading = state.value.updateLoading,
            updateInfo = state.value.updateInfo,
            listState = listState,
            checkForUpdate = screenModel::onEvent,
            navigateToBrowserPage = screenModel::onEvent,
            dismissDialog = screenModel::onEvent,
            navigateToLicenses = {
                navigator.push(LicensesScreen)
            },
            navigateToCredits = {
                navigator.push(CreditsScreen)
            },
            navigateBack = {
                navigator.pop()
            }
        )
    }
}