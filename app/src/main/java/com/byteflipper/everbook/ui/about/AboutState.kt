package com.byteflipper.everbook.ui.about

import androidx.compose.runtime.Immutable
import com.byteflipper.everbook.data.remote.dto.LatestReleaseInfo
import com.byteflipper.everbook.domain.util.Dialog

@Immutable
data class AboutState(
    val dialog: Dialog? = null,
    val updateChecked: Boolean = false,
    val updateLoading: Boolean = false,
    val updateInfo: LatestReleaseInfo? = null
)