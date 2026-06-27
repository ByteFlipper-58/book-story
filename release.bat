@echo off
setlocal

set DO_CLEAN=0
set BUILD_AAB=0

:: parse args
for %%a in (%*) do (
    if /i "%%a"=="-Clean"   set DO_CLEAN=1
    if /i "%%a"=="-clean"   set DO_CLEAN=1
    if /i "%%a"=="--clean"  set DO_CLEAN=1

    if /i "%%a"=="-aab"     set BUILD_AAB=1
    if /i "%%a"=="--aab"    set BUILD_AAB=1
)

if not exist "keystore.properties" (
    echo [WARN] keystore.properties not found - APKs will NOT be signed.
)

if %DO_CLEAN% equ 1 (
    echo --- Cleaning ---
    call gradlew --console=plain --no-configuration-cache clean
    if %errorlevel% neq 0 exit /b %errorlevel%
)

echo --- Building APKs ---
call gradlew --console=plain --no-configuration-cache ^
    :app:assembleEverbookRelease ^
    :app:assemblePlayStoreRelease ^
    :app:assembleRuStoreRelease

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED
    exit /b %errorlevel%
)

if %BUILD_AAB% equ 1 (
    echo --- Building AABs ---
    call gradlew --console=plain --no-configuration-cache ^
        :app:bundleEverbookRelease ^
        :app:bundlePlayStoreRelease ^
        :app:bundleRuStoreRelease

    if %errorlevel% neq 0 (
        echo.
        echo AAB BUILD FAILED
        exit /b %errorlevel%
    )
)

echo.
echo Done. APKs:
dir /s /b app\build\outputs\apk\*.apk 2>nul

if %BUILD_AAB% equ 1 (
    echo.
    echo AABs:
    dir /s /b app\build\outputs\bundle\*.aab 2>nul
)