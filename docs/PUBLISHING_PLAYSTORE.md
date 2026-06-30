# Publishing to Google Play (`playStore` flavor)

How to configure, build, and release the **`playStore`** flavor to Google Play.

- RuStore build → [PUBLISHING_RUSTORE.md](PUBLISHING_RUSTORE.md)
- Flavor overview → [README](../README.md#-build-variants)
- Store listing → https://play.google.com/store/apps/details?id=com.byteflipper.everbook

## Contents
1. [What's in this flavor](#1-whats-in-this-flavor)
2. [Configuration](#2-configuration)
3. [Build](#3-build)
4. [Upload to Play Console](#4-upload-to-play-console)
5. [Checklist](#5-checklist)
6. [Useful links](#6-useful-links)

---

## 1. What's in this flavor

| Concern | Implementation |
| --- | --- |
| Ads | Google AdMob |
| Analytics / Crashlytics | Firebase |
| Consent | Google UMP — Consent Mode v2 |
| In-app update / review | Play In-App Update + Review |
| Billing | Play Billing |
| Translation | On-device ML Kit |

Code: `app/src/playStore` (+ shared ad code in `app/src/sharedAds`).
`app/src/playStoreDebug` is a debug-only overlay. `applicationId` is `com.byteflipper.everbook`.

---

## 2. Configuration

Set these up before building a release. Items marked **required** block the build if missing.

### 2.1 `google-services.json` — required

Without it, every `playStore` build fails (Firebase plugins apply only to this flavor).

1. [Firebase Console](https://console.firebase.google.com/) → your project → **Add app → Android**.
2. Package name **`com.byteflipper.everbook`**.
3. Download `google-services.json` and place it at `app/src/playStore/google-services.json`.

> ⚠️ This file is currently committed (`.gitignore` only covers `/app/google-services.json`). It's
> client config, not a private key, but if you want it out of a public repo add
> `app/src/playStore/google-services.json` to `.gitignore`.

### 2.2 AdMob — App ID + ad units

In the [AdMob console](https://apps.admob.com/) create the app + ad units, then:

- **App ID** → `release` build type in [`app/build.gradle.kts`](../app/build.gradle.kts):
  ```kotlin
  manifestPlaceholders["adMobAppId"] = "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
  ```
  Keep `debug` on Google's test id `ca-app-pub-3940256099942544~3347511713`.
- **Ad unit ids** → [`app/src/playStore/res/values/admob.xml`](../app/src/playStore/res/values/admob.xml):

  | Placement | Resource | [Test unit](https://developers.google.com/admob/android/test-ads) (dev) |
  | --- | --- | --- |
  | Interstitial | `admob_interstitial_unit_id` | `ca-app-pub-3940256099942544/1033173712` |
  | Native (text) | `admob_native_reader_text_unit_id` | `ca-app-pub-3940256099942544/2247696110` |
  | Native (PDF) | `admob_native_reader_pdf_unit_id` | `ca-app-pub-3940256099942544/2247696110` |

  Use test ids in dev; swap to your real ids only for release.

### 2.3 Firebase Remote Config — ad behavior

Add one parameter per row in [Firebase Console → Remote Config](https://console.firebase.google.com/),
using the exact key name and type, then **Publish**. Keys/defaults are defined in
[`AdRemoteConfigConstants.kt`](../app/src/sharedAds/java/com/byteflipper/everbook/domain/config/AdRemoteConfigConstants.kt)
and shared with RuStore — keep both consoles in sync.

Missing keys or a failed fetch fall back to the default; out-of-range values are clamped.
Kill switch: `ads_enabled = false`.

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

Generate the keystore once and keep it forever:

```bash
keytool -genkey -v -keystore release.keystore -alias everbook \
        -keyalg RSA -keysize 2048 -validity 10000
```

Values may also come from Gradle `-P` properties or env vars. Without a resolvable keystore,
release builds are unsigned and Play rejects them.

### 2.5 Version

Bump in `defaultConfig` of [`app/build.gradle.kts`](../app/build.gradle.kts):

```kotlin
versionCode = 15        // must strictly increase for every Play upload
versionName = "1.6.0"
```

---

## 3. Build

Google Play requires an **App Bundle (`.aab`)**.

```bash
./gradlew bundlePlayStoreRelease     # release bundle for Play
./gradlew assemblePlayStoreRelease   # release APK (sideload/testing)
./gradlew assemblePlayStoreDebug     # debug APK
```

Output: `app/build/outputs/bundle/playStoreRelease/app-playStore-release.aab`

> Windows: [`release.bat`](../release.bat) builds release APKs for all flavors (`-aab` for bundles,
> `-clean` to clean first).

Release builds run R8 with `proguard-rules.pro` and ship native debug symbols
(`debugSymbolLevel = "FULL"`).

---

## 4. Upload to Play Console

1. [Play Console](https://play.google.com/console/) → your app → **Production** (or a testing track first) → **Create new release**.
2. Upload the `.aab`. With [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) (recommended) Google re-signs with the app key. Native debug symbols are embedded for crash symbolication.
3. **Release notes** — source: `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt` (e.g. `…/en-US/changelogs/15.txt`).
4. Complete **[Data safety](https://support.google.com/googleplay/android-developer/answer/10787469)**, **Ads**, and **Content rating** forms (this flavor has AdMob + Firebase).
5. Submit for review.

Listing text and screenshots live under `fastlane/metadata/android/` (locales: `en-US`, `uk`).

**Privacy policy** for this flavor: `app/src/playStore/assets/privacy_policy.html` — must describe
Firebase + AdMob + UMP. Keep the URL in the Play listing in sync.

---

## 5. Checklist

- [ ] `versionCode` increased, `versionName` updated
- [ ] `google-services.json` is the production Firebase project
- [ ] Real AdMob App ID + ad unit ids (not test)
- [ ] Remote Config parameters published (§2.3)
- [ ] `keystore.properties` resolves a valid keystore (build is signed)
- [ ] Changelog at `fastlane/metadata/android/*/changelogs/<versionCode>.txt`
- [ ] `bundlePlayStoreRelease` built and tested on a device
- [ ] Data safety / Ads / Content rating forms reflect AdMob + Firebase
- [ ] Privacy policy URL up to date

---

## 6. Useful links

- [Play Console](https://play.google.com/console/) · [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) · [Data safety form](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Firebase Console](https://console.firebase.google.com/) · [Remote Config docs](https://firebase.google.com/docs/remote-config)
- [AdMob Console](https://apps.admob.com/) · [AdMob test ads](https://developers.google.com/admob/android/test-ads)
- [Play In-App Update](https://developer.android.com/guide/playcore/in-app-updates) · [Play In-App Review](https://developer.android.com/guide/playcore/in-app-review)
- [User Messaging Platform (UMP)](https://developers.google.com/admob/android/privacy)
