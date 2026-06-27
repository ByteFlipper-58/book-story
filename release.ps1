param([switch] $Bundle, [switch] $Clean)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not (Test-Path keystore.properties)) { Write-Warning "keystore.properties not found - APKs will NOT be signed." }
if ($Clean) { ./gradlew --console=plain --no-configuration-cache clean }

if ($Bundle) {
    ./gradlew --console=plain --no-configuration-cache `
        :app:bundleEverbookRelease :app:bundlePlayStoreRelease :app:bundleRuStoreRelease
    if ($LASTEXITCODE) { exit $LASTEXITCODE }
    Write-Host "`nDone. AABs:" -ForegroundColor Green
    Get-ChildItem app/build/outputs/bundle -Recurse -Filter *.aab | % FullName
} else {
    ./gradlew --console=plain --no-configuration-cache `
        :app:assembleEverbookRelease :app:assemblePlayStoreRelease :app:assembleRuStoreRelease
    if ($LASTEXITCODE) { Write-Host "`nBUILD FAILED" -ForegroundColor Red; exit $LASTEXITCODE }
    Write-Host "`nDone. APKs:" -ForegroundColor Green
    Get-ChildItem app/build/outputs/apk -Recurse -Filter *.apk | % FullName
}
