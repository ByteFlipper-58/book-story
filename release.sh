#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
BUILD_AAB=false; DO_CLEAN=false
for a in "$@"; do case "$a" in --aab) BUILD_AAB=true ;; --clean) DO_CLEAN=true ;; esac; done

[ ! -f keystore.properties ] && echo "⚠  keystore.properties not found — APKs will NOT be signed."
$DO_CLEAN && { echo "--- Cleaning ---"; ./gradlew --console=plain --no-configuration-cache clean; }

if $BUILD_AAB; then
  echo "--- Building AABs ---"
  ./gradlew --console=plain --no-configuration-cache \
    :app:bundleEverbookRelease :app:bundlePlayStoreRelease :app:bundleRuStoreRelease
  echo; echo "Done. AABs:"; find app/build/outputs/bundle -name "*.aab" -type f
else
  echo "--- Building APKs ---"
  ./gradlew --console=plain --no-configuration-cache \
    :app:assembleEverbookRelease :app:assemblePlayStoreRelease :app:assembleRuStoreRelease
  echo; echo "Done. APKs:"; find app/build/outputs/apk -name "*.apk" -type f
fi
