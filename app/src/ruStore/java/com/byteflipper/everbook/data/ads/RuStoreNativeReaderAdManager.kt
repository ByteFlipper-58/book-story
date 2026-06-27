/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.ads

import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ads.AdFormat
import com.byteflipper.everbook.domain.ads.AdSessionController
import com.byteflipper.everbook.domain.config.ReaderNativeAdConfig
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentPlacement
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentState
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.MediaView
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdView
import com.yandex.mobile.ads.nativeads.Rating
import com.yandex.mobile.ads.nativeads.NativeAdViewBinder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * In-reader native ads backed by the Yandex Mobile Ads SDK. The placement / session pacing logic
 * matches the Play Store flavor; only ad loading ([NativeAdLoader]) and rendering
 * ([NativeAdViewBinder]) are Yandex-specific.
 */
class RuStoreNativeReaderAdManager @Inject constructor(
    private val adSessionController: AdSessionController,
    private val remoteFeatureConfig: RemoteFeatureConfig,
    private val privacyConsentManager: PrivacyConsentManager
) : ReaderInlineContentController {
    private val _state = MutableStateFlow(ReaderInlineContentState())

    private var adsEnabled = false
    private var config = ReaderNativeAdConfig()
    private var textSessionStartUnit: Int? = null
    private var textLastBreakUnit: Int? = null
    private var textShownThisSession = 0
    private var pdfSessionStartPage: Int? = null
    private var pdfLastBreakPage: Int? = null
    private var pdfShownThisSession = 0
    private var loading = false
    private var nextRetryAtMillis = 0L
    private var consecutiveFailures = 0
    private var nextBreakId = 0L
    private var pendingNativeAdMode: ReaderInlineContentMode? = null
    private var pendingNativeAd: NativeAd? = null
    private var configurationJob: Job? = null
    private var nativeAdLoader: NativeAdLoader? = null
    private val inlineAds = linkedMapOf<Long, NativeAd>()
    private val inlineAdModes = linkedMapOf<Long, ReaderInlineContentMode>()

    override val state: StateFlow<ReaderInlineContentState> = _state

    override fun configure(activity: ComponentActivity) {
        configurationJob?.cancel()
        configurationJob = activity.lifecycleScope.launch {
            combine(
                remoteFeatureConfig.adsEnabled,
                remoteFeatureConfig.readerNativeAdConfig,
                remoteFeatureConfig.adSessionConfig
            ) { adsEnabled, nativeAdConfig, adSessionConfig ->
                adSessionController.configure(adSessionConfig)
                adsEnabled to nativeAdConfig
            }.collectLatest { (adsEnabled, nativeAdConfig) ->
                this@RuStoreNativeReaderAdManager.adsEnabled = adsEnabled && nativeAdConfig.enabled
                this@RuStoreNativeReaderAdManager.config = nativeAdConfig

                if (!this@RuStoreNativeReaderAdManager.adsEnabled) {
                    clearAd()
                    _state.value = ReaderInlineContentState()
                    return@collectLatest
                }

                privacyConsentManager.requestConsentIfNeeded(activity) {}
            }
        }
    }

    override fun onReaderProgress(
        activity: ComponentActivity,
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int,
        lastProgressUnit: Int,
        readerAvailableForInlineContent: Boolean
    ) {
        if (!adsEnabled || maxPerSession(mode) <= shownThisSession(mode)) return
        if (!privacyConsentManager.canRequestAds(activity)) return
        if (!readerAvailableForInlineContent) return

        ensureSessionStart(mode, progressUnit)

        val eligibilityProgressUnit = eligibilityProgressUnit(
            mode = mode,
            progressUnit = progressUnit,
            visibleEndProgressUnit = visibleEndProgressUnit
        )
        val baseUnit = lastBreakUnit(mode) ?: sessionStartUnit(mode) ?: eligibilityProgressUnit
        val minProgressUnits = minProgressUnits(mode)
        val progressDelta = (eligibilityProgressUnit - baseUnit).coerceAtLeast(0)

        if (progressDelta >= minProgressUnits / 2) {
            load(activity, mode)
        }

        if (
            progressDelta >= minProgressUnits &&
            pendingNativeAd != null &&
            pendingNativeAdMode == mode &&
            adSessionController.canShow(AdFormat.READER_NATIVE)
        ) {
            val breakProgressUnit = futureBreakProgressUnit(
                mode = mode,
                progressUnit = progressUnit,
                visibleEndProgressUnit = visibleEndProgressUnit,
                lastProgressUnit = lastProgressUnit
            ) ?: return
            val breakId = nextBreakId++
            val ad = pendingNativeAd ?: return

            pendingNativeAd = null
            pendingNativeAdMode = null
            inlineAds[breakId] = ad
            inlineAdModes[breakId] = mode
            _state.value = _state.value.copy(
                placements = _state.value.placements + ReaderInlineContentPlacement(
                    id = breakId,
                    mode = mode,
                    progressUnit = breakProgressUnit
                )
            )
            adSessionController.recordShown(AdFormat.READER_NATIVE)
            android.util.Log.i(TAG, "Native placement queued ($mode at unit $breakProgressUnit)")
            setLastBreakUnit(mode, breakProgressUnit)
            incrementShownThisSession(mode)
            if (maxPerSession(mode) > shownThisSession(mode)) {
                load(activity, mode)
            }
        }
    }

    override fun createView(activity: ComponentActivity, placementId: Long): View? {
        val ad = inlineAds[placementId] ?: return null
        val mode = inlineAdModes[placementId] ?: return null

        return createNativeAdView(activity, ad, mode)
    }

    private fun createNativeAdView(
        activity: ComponentActivity,
        ad: NativeAd,
        mode: ReaderInlineContentMode
    ): View? {
        val context = activity
        val density = context.resources.displayMetrics.density
        val colors = nativeAdColors(context.resources.configuration)
        val isPdf = mode == ReaderInlineContentMode.PDF

        // Yandex SDK 8 NativeAdViewBinder binds into a NativeAdView. We create a NativeAdView,
        // pack our layout into it, and let the binder attach to the sub-views.
        val nativeAdView = NativeAdView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            if (isPdf) minimumHeight = (PDF_NATIVE_MIN_HEIGHT_DP * density).toInt()
            val pad = ((if (isPdf) 20 else 16) * density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        val label = TextView(context).apply {
            text = context.getString(R.string.native_ad_label)
            textSize = 12f
            setTextColor(colors.muted)
        }
        val mediaView = MediaView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ((if (isPdf) PDF_NATIVE_MEDIA_HEIGHT_DP else TEXT_NATIVE_MEDIA_HEIGHT_DP) * density)
                    .toInt()
            ).apply {
                topMargin = (8 * density).toInt()
                bottomMargin = (10 * density).toInt()
            }
        }
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val icon = ImageView(context).apply {
            val iconSize = ((if (isPdf) 48 else 40) * density).toInt()
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                rightMargin = (10 * density).toInt()
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        val titleColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        val title = TextView(context).apply {
            textSize = if (isPdf) 18f else 16f
            setTextColor(colors.primary)
        }
        // Yandex marks a subset of assets as "required" per creative (e.g. domain, age, price);
        // the binder must supply a view for every required asset or bindNativeAd() returns Failure.
        // We register all common assets and pack the secondary ones into compact rows so empty
        // ones collapse instead of leaving blank lines.
        val sponsored = TextView(context).apply {
            textSize = 12f
            setTextColor(colors.muted)
        }
        val domain = TextView(context).apply {
            textSize = 12f
            setTextColor(colors.muted)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = (6 * density).toInt() }
        }
        val age = TextView(context).apply {
            textSize = 12f
            setTextColor(colors.muted)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = (6 * density).toInt() }
        }
        val favicon = ImageView(context).apply {
            val faviconSize = (16 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(faviconSize, faviconSize).apply {
                rightMargin = (6 * density).toInt()
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        val feedback = ImageView(context).apply {
            val feedbackSize = (20 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(feedbackSize, feedbackSize)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        val metaRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val body = TextView(context).apply {
            textSize = 14f
            setTextColor(colors.secondary)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (8 * density).toInt()
            }
        }
        val price = TextView(context).apply {
            textSize = 13f
            setTextColor(colors.secondary)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val rating = YandexRatingBar(context).apply {
            numStars = 5
            stepSize = 0.5f
            setIsIndicator(true)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = (8 * density).toInt() }
        }
        val reviewCount = TextView(context).apply {
            textSize = 12f
            setTextColor(colors.muted)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = (8 * density).toInt() }
        }
        val priceRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = (6 * density).toInt() }
        }
        val warning = TextView(context).apply {
            textSize = 11f
            setTextColor(colors.muted)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        }
        val callToAction = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (12 * density).toInt()
            }
        }

        metaRow.addView(favicon)
        metaRow.addView(sponsored)
        metaRow.addView(domain)
        metaRow.addView(age)
        priceRow.addView(price)
        priceRow.addView(rating)
        priceRow.addView(reviewCount)
        titleColumn.addView(title)
        titleColumn.addView(metaRow)
        header.addView(icon)
        header.addView(titleColumn)
        header.addView(feedback)
        container.addView(label)
        container.addView(mediaView)
        container.addView(header)
        container.addView(body)
        container.addView(priceRow)
        container.addView(warning)
        container.addView(callToAction)

        val binder = NativeAdViewBinder.Builder(nativeAdView)
            .setMediaView(mediaView)
            .setIconView(icon)
            .setFaviconView(favicon)
            .setTitleView(title)
            .setSponsoredView(sponsored)
            .setDomainView(domain)
            .setAgeView(age)
            .setBodyView(body)
            .setPriceView(price)
            .setRatingView(rating)
            .setReviewCountView(reviewCount)
            .setWarningView(warning)
            .setFeedbackView(feedback)
            .setCallToActionView(callToAction)
            .build()

        return when (val result = ad.bindNativeAd(binder)) {
            is AdBindingResult.Success -> {
                android.util.Log.i(TAG, "Native bound & rendered ($mode)")
                nativeAdView.addView(container)
                nativeAdView
            }

            is AdBindingResult.Failure -> {
                android.util.Log.w(
                    TAG,
                    "Native ad binding failed: missingAsset=${result.missingAssetName}",
                    result.exception
                )
                null
            }
        }
    }

    override fun resetSession() {
        textSessionStartUnit = null
        textLastBreakUnit = null
        textShownThisSession = 0
        pdfSessionStartPage = null
        pdfLastBreakPage = null
        pdfShownThisSession = 0
        _state.value = ReaderInlineContentState()
        clearAd()
    }

    private fun load(activity: ComponentActivity, mode: ReaderInlineContentMode) {
        if (!adsEnabled || loading) return
        if (android.os.SystemClock.elapsedRealtime() < nextRetryAtMillis) return
        if (pendingNativeAd != null) {
            if (pendingNativeAdMode == mode) return

            pendingNativeAd = null
            pendingNativeAdMode = null
        }

        val adUnitId = activity.getString(
            when (mode) {
                ReaderInlineContentMode.TEXT -> R.string.yandex_native_reader_text_unit_id
                ReaderInlineContentMode.PDF -> R.string.yandex_native_reader_pdf_unit_id
            }
        )
        if (adUnitId.isBlank()) return

        val loader = nativeAdLoader ?: NativeAdLoader(activity).also { nativeAdLoader = it }

        loading = true
        android.util.Log.d(TAG, "Native requesting ($mode, unit=$adUnitId)")
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : NativeAdLoadListener {
                override fun onAdLoaded(nativeAd: NativeAd) {
                    pendingNativeAdMode = mode
                    pendingNativeAd = nativeAd
                    loading = false
                    consecutiveFailures = 0
                    nextRetryAtMillis = 0L
                    android.util.Log.i(TAG, "Native loaded ($mode)")
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    loading = false
                    consecutiveFailures += 1
                    // Exponential backoff capped at MAX_BACKOFF_MS to avoid spamming on "no fill".
                    val backoff = (BASE_BACKOFF_MS shl (consecutiveFailures - 1))
                        .coerceAtMost(MAX_BACKOFF_MS)
                    nextRetryAtMillis = android.os.SystemClock.elapsedRealtime() + backoff
                    android.util.Log.w(
                        TAG, "Native reader ad failed: ${error.code} ${error.description}; " +
                            "retry in ${backoff}ms"
                    )
                }
            }
        )
    }

    private fun eligibilityProgressUnit(
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int
    ): Int {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> visibleEndProgressUnit.coerceAtLeast(progressUnit)
            ReaderInlineContentMode.PDF -> progressUnit
        }
    }

    private fun futureBreakProgressUnit(
        mode: ReaderInlineContentMode,
        progressUnit: Int,
        visibleEndProgressUnit: Int,
        lastProgressUnit: Int
    ): Int? {
        val endGuardUnits = when (mode) {
            ReaderInlineContentMode.TEXT -> config.textEndGuardUnits
            ReaderInlineContentMode.PDF -> config.pdfEndGuardPages.coerceAtMost(PDF_END_GUARD_PAGES_MAX)
        }
        val lookaheadUnits = when (mode) {
            ReaderInlineContentMode.TEXT -> config.textLookaheadUnits
            ReaderInlineContentMode.PDF -> config.pdfLookaheadPages
        }
        val maxBreakProgressUnit = lastProgressUnit - endGuardUnits
        val visibleEndUnit = visibleEndProgressUnit.coerceAtLeast(progressUnit)
        val breakProgressUnit = visibleEndUnit + lookaheadUnits

        return breakProgressUnit.takeIf { it <= maxBreakProgressUnit }
    }

    private fun clearAd() {
        loading = false
        pendingNativeAdMode = null
        pendingNativeAd = null
        inlineAds.clear()
        inlineAdModes.clear()
    }

    private fun nativeAdColors(configuration: Configuration): NativeAdColors {
        val isNightMode = configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        return NativeAdColors(
            primary = if (isNightMode) Color.WHITE else Color.BLACK,
            secondary = if (isNightMode) Color.LTGRAY else Color.DKGRAY,
            muted = if (isNightMode) Color.LTGRAY else Color.GRAY
        )
    }

    private fun ensureSessionStart(mode: ReaderInlineContentMode, progressUnit: Int) {
        when (mode) {
            ReaderInlineContentMode.TEXT -> {
                if (textSessionStartUnit == null) textSessionStartUnit = progressUnit
            }

            ReaderInlineContentMode.PDF -> {
                if (pdfSessionStartPage == null) pdfSessionStartPage = progressUnit
            }
        }
    }

    private fun sessionStartUnit(mode: ReaderInlineContentMode): Int? {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> textSessionStartUnit
            ReaderInlineContentMode.PDF -> pdfSessionStartPage
        }
    }

    private fun lastBreakUnit(mode: ReaderInlineContentMode): Int? {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> textLastBreakUnit
            ReaderInlineContentMode.PDF -> pdfLastBreakPage
        }
    }

    private fun setLastBreakUnit(mode: ReaderInlineContentMode, progressUnit: Int) {
        when (mode) {
            ReaderInlineContentMode.TEXT -> textLastBreakUnit = progressUnit
            ReaderInlineContentMode.PDF -> pdfLastBreakPage = progressUnit
        }
    }

    private fun shownThisSession(mode: ReaderInlineContentMode): Int {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> textShownThisSession
            ReaderInlineContentMode.PDF -> pdfShownThisSession
        }
    }

    private fun incrementShownThisSession(mode: ReaderInlineContentMode) {
        when (mode) {
            ReaderInlineContentMode.TEXT -> textShownThisSession += 1
            ReaderInlineContentMode.PDF -> pdfShownThisSession += 1
        }
    }

    private fun minProgressUnits(mode: ReaderInlineContentMode): Int {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> if (textShownThisSession == 0) {
                config.textFirstMinUnits
            } else {
                config.textNextMinUnits
            }

            ReaderInlineContentMode.PDF -> if (pdfShownThisSession == 0) {
                config.pdfFirstMinPages
            } else {
                config.pdfNextMinPages
            }
        }
    }

    private fun maxPerSession(mode: ReaderInlineContentMode): Int {
        return when (mode) {
            ReaderInlineContentMode.TEXT -> config.textMaxPerSession
            ReaderInlineContentMode.PDF -> config.pdfMaxPerSession
        }
    }

    companion object {
        private const val TAG = "NativeReaderAds"
        private const val PDF_END_GUARD_PAGES_MAX = 2
        private const val PDF_NATIVE_MIN_HEIGHT_DP = 520
        private const val PDF_NATIVE_MEDIA_HEIGHT_DP = 280
        private const val TEXT_NATIVE_MEDIA_HEIGHT_DP = 180
        private const val BASE_BACKOFF_MS = 30_000L
        private const val MAX_BACKOFF_MS = 600_000L
    }
}

private data class NativeAdColors(
    val primary: Int,
    val secondary: Int,
    val muted: Int
)

/**
 * Yandex's [NativeAdViewBinder.setRatingView] requires a view implementing [Rating]. [RatingBar]
 * already exposes matching `getRating`/`setRating` methods, so subclassing it satisfies the
 * interface without extra logic. Uses the small indicator style for a compact star row.
 */
private class YandexRatingBar(context: android.content.Context) : RatingBar(
    context,
    null,
    android.R.attr.ratingBarStyleSmall
), Rating
