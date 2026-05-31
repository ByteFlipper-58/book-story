/*
 * EverBook — a modified fork of Book's Story, a free and open-source Material You eBook reader.
 * Copyright (C) 2024-2025 Acclorite
 * Modified by ByteFlipper for EverBook
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.byteflipper.everbook.data.ads

import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.byteflipper.everbook.R
import com.byteflipper.everbook.domain.ads.AdFormat
import com.byteflipper.everbook.domain.ads.AdSessionController
import com.byteflipper.everbook.domain.config.RemoteFeatureConfig
import com.byteflipper.everbook.domain.config.ReaderNativeAdConfig
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentController
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentMode
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentPlacement
import com.byteflipper.everbook.domain.distribution.ReaderInlineContentState
import com.byteflipper.everbook.domain.privacy.PrivacyConsentManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PlayStoreNativeReaderAdManager @Inject constructor(
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
    private var isMobileAdsInitialized = false
    private var loading = false
    private var nextBreakId = 0L
    private var pendingNativeAdMode: ReaderInlineContentMode? = null
    private var pendingNativeAd: NativeAd? = null
    private val inlineAds = linkedMapOf<Long, NativeAd>()

    override val state: StateFlow<ReaderInlineContentState> = _state

    override fun configure(activity: ComponentActivity) {
        val config = remoteFeatureConfig.readerNativeAdConfig.value

        adSessionController.configure(remoteFeatureConfig.adSessionConfig.value)
        this.adsEnabled = remoteFeatureConfig.adsEnabled.value && config.enabled
        this.config = config

        if (!this.adsEnabled) {
            clearAd()
            _state.value = ReaderInlineContentState()
            return
        }

        privacyConsentManager.requestConsentIfNeeded(activity) {}
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

        val baseUnit = lastBreakUnit(mode) ?: sessionStartUnit(mode) ?: progressUnit
        val minProgressUnits = minProgressUnits(mode)
        val progressDelta = (progressUnit - baseUnit).coerceAtLeast(0)

        if (progressDelta >= minProgressUnits / 2) {
            initializeAndLoad(activity, mode)
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
            _state.value = _state.value.copy(
                placements = _state.value.placements + ReaderInlineContentPlacement(
                    id = breakId,
                    mode = mode,
                    progressUnit = breakProgressUnit
                )
            )
            adSessionController.recordShown(AdFormat.READER_NATIVE)
            setLastBreakUnit(mode, breakProgressUnit)
            incrementShownThisSession(mode)
            if (maxPerSession(mode) > shownThisSession(mode)) {
                initializeAndLoad(activity, mode)
            }
        }
    }

    override fun createView(activity: ComponentActivity, placementId: Long): View? {
        val ad = inlineAds[placementId] ?: return null
        val context = activity
        val density = context.resources.displayMetrics.density
        val isNightMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val primaryTextColor = if (isNightMode) Color.WHITE else Color.BLACK
        val secondaryTextColor = if (isNightMode) Color.LTGRAY else Color.DKGRAY
        val mutedTextColor = if (isNightMode) Color.LTGRAY else Color.GRAY
        val adView = NativeAdView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (16 * density).toInt(),
                (14 * density).toInt(),
                (16 * density).toInt(),
                (14 * density).toInt()
            )
        }
        val label = TextView(context).apply {
            text = context.getString(R.string.native_ad_label)
            textSize = 12f
            setTextColor(mutedTextColor)
        }
        val mediaView = MediaView(context).apply {
            visibility = if (ad.mediaContent != null) View.VISIBLE else View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (180 * density).toInt()
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
            val iconDrawable = ad.icon?.drawable
            visibility = if (iconDrawable == null) View.GONE else View.VISIBLE
            setImageDrawable(iconDrawable)
            layoutParams = LinearLayout.LayoutParams(
                (40 * density).toInt(),
                (40 * density).toInt()
            ).apply {
                rightMargin = (10 * density).toInt()
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        val headline = TextView(context).apply {
            text = ad.headline.orEmpty()
            textSize = 16f
            setTextColor(primaryTextColor)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        val advertiser = TextView(context).apply {
            text = ad.advertiser.orEmpty()
            textSize = 12f
            setTextColor(mutedTextColor)
            visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        val body = TextView(context).apply {
            text = ad.body.orEmpty()
            textSize = 14f
            setTextColor(secondaryTextColor)
            visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (8 * density).toInt()
            }
        }
        val callToAction = Button(context).apply {
            text = ad.callToAction.orEmpty()
            visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (12 * density).toInt()
            }
        }

        container.addView(label)
        container.addView(mediaView)
        header.addView(icon)
        header.addView(headline)
        container.addView(header)
        container.addView(advertiser)
        container.addView(body)
        container.addView(callToAction)

        adView.addView(container)
        adView.mediaView = mediaView
        adView.iconView = icon
        adView.headlineView = headline
        adView.advertiserView = advertiser
        adView.bodyView = body
        adView.callToActionView = callToAction
        adView.setNativeAd(ad)

        return adView
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
        if (!adsEnabled || loading || pendingNativeAd != null) return

        val adUnitId = activity.getString(
            when (mode) {
                ReaderInlineContentMode.TEXT -> R.string.admob_native_reader_text_unit_id
                ReaderInlineContentMode.PDF -> R.string.admob_native_reader_pdf_unit_id
            }
        )
        if (adUnitId.isBlank()) return

        loading = true
        AdLoader.Builder(activity, adUnitId)
            .forNativeAd { ad ->
                pendingNativeAd?.destroy()
                pendingNativeAdMode = mode
                pendingNativeAd = ad
                loading = false
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .setVideoOptions(
                        VideoOptions.Builder()
                            .setStartMuted(true)
                            .build()
                    )
                    .build()
            )
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loading = false
                        Log.w(TAG, "Native reader ad failed: ${error.code} ${error.message}")
                    }
                }
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    private fun initializeAndLoad(
        activity: ComponentActivity,
        mode: ReaderInlineContentMode
    ) {
        if (!isMobileAdsInitialized) {
            isMobileAdsInitialized = true
            MobileAds.initialize(activity) {
                load(activity, mode)
            }
            return
        }

        load(activity, mode)
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
        pendingNativeAd?.destroy()
        pendingNativeAdMode = null
        pendingNativeAd = null
        inlineAds.values.forEach { it.destroy() }
        inlineAds.clear()
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
    }
}
