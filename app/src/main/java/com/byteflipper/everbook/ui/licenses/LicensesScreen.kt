package com.byteflipper.everbook.ui.licenses

import android.os.Parcelable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.navigator.Screen
import com.byteflipper.everbook.presentation.core.components.top_bar.collapsibleTopAppBarScrollBehavior
import com.byteflipper.everbook.presentation.licenses.LicensesContent
import com.byteflipper.everbook.presentation.navigator.LocalNavigator
import com.byteflipper.everbook.ui.license_info.LicenseInfoScreen

@Parcelize
object LicensesScreen : Screen, Parcelable {

    @IgnoredOnParcel
    private var initialIndex = 0

    @IgnoredOnParcel
    private var initialOffset = 0

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val context = LocalContext.current

        val (scrollBehavior, listState) = TopAppBarDefaults.collapsibleTopAppBarScrollBehavior(
            listState = rememberLazyListState(
                initialFirstVisibleItemIndex = initialIndex,
                initialFirstVisibleItemScrollOffset = initialOffset
            )
        )
        val licenses = remember {
            derivedStateOf {
                Libs.Builder().withJson(context, R.raw.aboutlibraries).build()
                    .libraries.sortedBy { it.openSource }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                initialIndex = listState.firstVisibleItemIndex
                initialOffset = listState.firstVisibleItemScrollOffset
            }
        }

        LicensesContent(
            licenses = licenses.value,
            scrollBehavior = scrollBehavior,
            listState = listState,
            navigateToLicenseInfo = {
                navigator.push(LicenseInfoScreen(it.uniqueId))
            },
            navigateBack = {
                navigator.pop()
            }
        )
    }
}