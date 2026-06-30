# Publishing to RuStore (`ruStore` flavor)

How to configure, build, and release the **`ruStore`** flavor to [RuStore](https://www.rustore.ru/).

- Google Play build → [PUBLISHING_PLAYSTORE.md](PUBLISHING_PLAYSTORE.md)
- Flavor overview → [README](../README.md#-build-variants)
- Store listing → https://www.rustore.ru/catalog/app/com.byteflipper.everbook

## Contents
1. [What's in this flavor](#1-whats-in-this-flavor)
2. [Configuration](#2-configuration)
3. [Build](#3-build)
4. [Upload to RuStore Console](#4-upload-to-rustore-console)
5. [Checklist](#5-checklist)
6. [Useful links](#6-useful-links)

---

## 1. What's in this flavor

| Concern | Implementation |
| --- | --- |
| Ads | Yandex Mobile Ads |
| In-app update / review | RuStore App Update + Review SDK |
| Remote config | RuStore Remote Config SDK |
| Consent | Custom `RuStorePrivacyConsentManager` (no Google UMP) |
| Translation | On-device ML Kit |

**No Google services** — no `google-services.json`; the Firebase/Crashlytics plugins are skipped
for `ruStore` tasks, and ML Kit runs on-device without Google Play Services.

Code: `app/src/ruStore` (+ shared ad code in `app/src/sharedAds`). `app/src/ruStoreDebug` is a
debug-only overlay. `applicationId` is `com.byteflipper.everbook`. Key entry point:
`data/distribution/RuStoreDistributionStartup.kt`.

---

## 2. Configuration

Set these up before building a release.

### 2.1 RuStore App ID

Register the app in the [RuStore Console](https://console.rustore.ru/) under package
`com.byteflipper.everbook`, copy its **App ID** (a UUID), and set it in the `ruStore` flavor in
[`app/build.gradle.kts`](../app/build.gradle.kts):

```kotlin
create("ruStore") {
    dimension = "distribution"
    buildConfigField("String", "RU_STORE_APP_ID", "\"<your-rustore-app-id>\"")
}
```

Consumed by `RuStoreDistributionStartup.kt` to init the update / review / remote-config SDKs.

### 2.2 Yandex Mobile Ads — App ID + ad units

In the [Yandex Ads cabinet](https://partner.yandex.ru/) create the app + ad units, then:

- **App ID** → [`app/src/ruStore/AndroidManifest.xml`](../app/src/ruStore/AndroidManifest.xml):
  ```xml
  <meta-data
      android:name="com.yandex.mobile.ads.APPLICATION_ID"
      android:value="<your-yandex-app-id>" />
  ```
- **Ad unit ids** (form `R-M-<appId>-<n>`) → [`app/src/ruStore/res/values/yandex_ads.xml`](../app/src/ruStore/res/values/yandex_ads.xml):

  | Placement | Resource | [Demo unit](https://yandex.ru/dev/mobile-ads/doc/dg/concepts/demo-ad-units.html) (dev) |
  | --- | --- | --- |
  | Interstitial | `yandex_interstitial_unit_id` | `demo-interstitial-yandex` |
  | Native (text) | `yandex_native_reader_text_unit_id` | `demo-native-content-yandex` |
  | Native (PDF) | `yandex_native_reader_pdf_unit_id` | `demo-native-content-yandex` |

  Use demo ids in dev; swap to your real ids only for release. Text and PDF reuse the same unit.

### 2.3 RuStore Remote Config — ad behavior

Add one parameter per row in **RuStore Console → Remote Config**, using the exact key name and
type, then publish. Keys/defaults are defined in
[`AdRemoteConfigConstants.kt`](../app/src/sharedAds/java/com/byteflipper/everbook/domain/config/AdRemoteConfigConstants.kt)
and **identical to the Play (Firebase) build** — keep both consoles in sync.

Missing keys or an unreachable config fall back to the default; out-of-range values are clamped.
Kill switch: `ads_enabled = false`. Unlike Play, there is no UMP/consent gate — defaults apply
immediately.

| Key | Type | Default | Range | Description |
| --- | --- | --- | --- | --- |
| `ads_enabled` | bool | `true` | — | Master switch for all ads. |
| `reader_entry_interstitial_ads_enabled` | bool | `true` | — | Interstitial when opening a book. |
| `reader_entry_interstitial_show_interval` | long | `4` | 1–20 | Show interstitial every N reader entries. |
| `ad_global_cooldown_seconds` | long | `120` | 0–86400 | Min seconds between any two ads. |
| `reader_native_ads_enabled` | bool | `true` | — | Native ads inside the reader. |
| `reader_native_text_first_min_units` | long | `3` | 1–1000 | Text: units before the first native ad. |
| `reader_native_text_next_min_units` | long | `8` | 1–1000 | Text: units between native ads. |
| `reader_native_text_max_per_session` | long | `8` | 0–20 | Text: max native ads per session. |
| `reader_native_text_end_guard_units` | long | `8` | 0–1000 | Text: no native ad within N units of the end. |
| `reader_native_text_lookahead_units` | long | `2` | 1–100 | Text: how far ahead the ad break is scheduled. |
| `reader_native_pdf_first_min_pages` | long | `6` | 1–1000 | PDF: pages before the first native ad. |
| `reader_native_pdf_next_min_pages` | long | `10` | 1–1000 | PDF: pages between native ads. |
| `reader_native_pdf_max_per_session` | long | `8` | 0–20 | PDF: max native ads per session. |
| `reader_native_pdf_end_guard_pages` | long | `2` | 0–100 | PDF: no native ad within N pages of the end. |
| `reader_native_pdf_lookahead_pages` | long | `1` | 1–100 | PDF: how far ahead the ad break is scheduled. |

### 2.4 Signing — required for release

Copy the template and fill it in (git-ignored — never commit it or the keystore):

```bash
cp keystore.properties.example keystore.properties
```

Same keystore as the other flavors. Values may also come from Gradle `-P` properties or env vars.
Without a resolvable keystore, release builds are unsigned.

> ⚠️ RuStore has **no** Play-style app signing — you upload an already-signed artifact and must
> keep this keystore for all future updates of the same package.

### 2.5 Version & SDK pins

Bump in `defaultConfig` of [`app/build.gradle.kts`](../app/build.gradle.kts):

```kotlin
versionCode = 15        // must strictly increase for each RuStore upload
versionName = "1.6.0"
```

RuStore/Yandex SDK versions are pinned in the same file:

```kotlin
platform("ru.rustore.sdk:bom:2026.06.01")   // → SDK 10.5.0
"com.yandex.android:mobileads:8.1.0"
```

---

## 3. Build

RuStore accepts both **APK** and **AAB** (APK is simplest).

```bash
./gradlew assembleRuStoreRelease   # release APK
./gradlew bundleRuStoreRelease     # release AAB
./gradlew assembleRuStoreDebug     # debug APK
```

Output: `app/build/outputs/apk/ruStore/release/app-ruStore-release.apk`

> Windows: [`release.bat`](../release.bat) builds release APKs for all flavors (`-aab` for bundles,
> `-clean` to clean first).

Release builds run R8 with `proguard-rules.pro`. If the RuStore/Yandex SDKs misbehave after
minification, check their keep-rules in `proguard-rules.pro`.

---

## 4. Upload to RuStore Console

1. [RuStore Console](https://console.rustore.ru/) → the app matching `RU_STORE_APP_ID` → **new version**.
2. Upload the signed `.apk` (or `.aab`); confirm `versionCode` is higher than the published one.
3. **Release notes** — reuse `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt` (prioritize Russian).
4. Complete age rating, category, and the ads / data-collection questionnaire (Yandex ads).
5. Submit for moderation.

**In-app update / review:** these only trigger for builds **installed from RuStore** with a matching
published `versionCode` — test against a real RuStore release, not a sideloaded APK.

**Privacy policy** for this flavor: `app/src/ruStore/assets/privacy_policy.html` — must describe
Yandex ads + RuStore SDKs (not Firebase/AdMob). Keep the URL in the RuStore listing in sync.

---

## 5. Checklist

- [ ] `versionCode` increased, `versionName` updated
- [ ] `RU_STORE_APP_ID` matches the app in RuStore Console
- [ ] Real Yandex App ID + ad unit ids in `yandex_ads.xml` (not demo)
- [ ] Remote Config parameters published (§2.3)
- [ ] `keystore.properties` resolves a valid keystore (build is signed)
- [ ] Changelog at `fastlane/metadata/android/*/changelogs/<versionCode>.txt` (RU prioritized)
- [ ] `assembleRuStoreRelease` built and launches (ads + update check) on a device
- [ ] Privacy policy describes Yandex ads + RuStore SDKs
- [ ] Ads / data-collection questionnaire completed in RuStore Console

---

## 6. Useful links

- [RuStore Console](https://console.rustore.ru/) · [RuStore developer docs](https://www.rustore.ru/help/sdk/)
- [RuStore App Update SDK](https://www.rustore.ru/help/sdk/application-update/) · [Review SDK](https://www.rustore.ru/help/sdk/reviews/) · [Remote Config SDK](https://www.rustore.ru/help/sdk/remote-config/)
- [Yandex Mobile Ads](https://yandex.ru/dev/mobile-ads/doc/intro/about-sdk.html) · [Demo ad units](https://yandex.ru/dev/mobile-ads/doc/dg/concepts/demo-ad-units.html)
