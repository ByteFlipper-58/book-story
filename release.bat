@echo off
setlocal EnableExtensions EnableDelayedExpansion

title Book's Story - Release Builder

set "FLAVORS="
set "BUILD_APK=0"
set "BUILD_AAB=0"
set "BUILD_TYPE="
set "BUILD_OUTPUT_TYPE="
set "DO_CLEAN=0"

echo.
echo ========================================
echo   Book's Story Release Builder
echo ========================================
echo.

:select_flavor
echo Select distribution:
echo   [1] Everbook  - no ads or Google services
echo   [2] Play Store - AdMob, Firebase and Google Play services
echo   [3] RuStore   - AdMob and RuStore services
echo   [4] All distributions
echo   [Q] Cancel
choice /c 1234Q /n /m "Choice"

if errorlevel 5 goto :cancel
if errorlevel 4 set "FLAVORS=Everbook PlayStore RuStore" & goto :select_artifact
if errorlevel 3 set "FLAVORS=RuStore" & goto :select_artifact
if errorlevel 2 set "FLAVORS=PlayStore" & goto :select_artifact
if errorlevel 1 set "FLAVORS=Everbook" & goto :select_artifact

goto :select_flavor

:select_artifact
echo.
echo Select output format:
echo   [1] APK
 echo   [2] AAB
 echo   [3] APK and AAB
 echo   [Q] Cancel
choice /c 123Q /n /m "Choice"

if errorlevel 4 goto :cancel
if errorlevel 3 set "BUILD_APK=1" & set "BUILD_AAB=1" & goto :select_build_type
if errorlevel 2 set "BUILD_AAB=1" & goto :select_build_type
if errorlevel 1 set "BUILD_APK=1" & goto :select_build_type

goto :select_artifact

:select_build_type
echo.
echo Select build type:
echo   [1] Release - signed, optimized and minified
echo   [2] Debug   - fast developer build, not minified
echo   [Q] Cancel
choice /c 12Q /n /m "Choice"

if errorlevel 3 goto :cancel
if errorlevel 2 set "BUILD_TYPE=Debug" & set "BUILD_OUTPUT_TYPE=debug" & goto :select_clean
if errorlevel 1 set "BUILD_TYPE=Release" & set "BUILD_OUTPUT_TYPE=release" & goto :select_clean

goto :select_build_type

:select_clean
echo.
choice /c YN /n /m "Run Gradle clean before building? [Y/N]"
if errorlevel 2 goto :confirm
if errorlevel 1 set "DO_CLEAN=1" & goto :confirm

goto :select_clean

:confirm
echo.
echo Summary:
echo   Distributions: %FLAVORS%
echo   Build type: %BUILD_TYPE%
if %BUILD_APK% equ 1 (echo   [x] APK) else (echo   [ ] APK)
if %BUILD_AAB% equ 1 (echo   [x] AAB) else (echo   [ ] AAB)
if %DO_CLEAN% equ 1 (echo   [x] Clean) else (echo   [ ] Clean)
echo.
choice /c YN /n /m "Start the release build? [Y/N]"
if errorlevel 2 goto :cancel

if not exist "keystore.properties" (
    echo.
    echo [WARN] keystore.properties not found - release artifacts will NOT be signed.
)

if %DO_CLEAN% equ 1 (
    echo.
    echo --- Cleaning ---
    call gradlew --console=plain --no-configuration-cache clean
    set "RESULT=!ERRORLEVEL!"
    if not "!RESULT!"=="0" goto :build_failed
)

if %BUILD_APK% equ 1 (
    set "APK_TASKS="
    for %%F in (%FLAVORS%) do set "APK_TASKS=!APK_TASKS! :app:assemble%%F%BUILD_TYPE%"

    echo.
    echo --- Building APKs ---
    call gradlew --console=plain --no-configuration-cache !APK_TASKS!
    set "RESULT=!ERRORLEVEL!"
    if not "!RESULT!"=="0" goto :build_failed
)

if %BUILD_AAB% equ 1 (
    set "AAB_TASKS="
    for %%F in (%FLAVORS%) do set "AAB_TASKS=!AAB_TASKS! :app:bundle%%F%BUILD_TYPE%"

    echo.
    echo --- Building AABs ---
    call gradlew --console=plain --no-configuration-cache !AAB_TASKS!
    set "RESULT=!ERRORLEVEL!"
    if not "!RESULT!"=="0" goto :build_failed
)

echo.
echo ========================================
echo   BUILD SUCCESSFUL
echo ========================================

if %BUILD_APK% equ 1 (
    echo.
    echo APKs:
    for %%F in (%FLAVORS%) do dir /b "app\build\outputs\apk\%%F\%BUILD_OUTPUT_TYPE%\*.apk" 2^>nul
)

if %BUILD_AAB% equ 1 (
    echo.
    echo AABs:
    for %%F in (%FLAVORS%) do dir /b "app\build\outputs\bundle\%%F\%BUILD_OUTPUT_TYPE%\*.aab" 2^>nul
)

echo.
pause
goto :end

:build_failed
echo.
echo ========================================
echo   BUILD FAILED (exit code !RESULT!)
echo ========================================
echo.
pause
exit /b !RESULT!

:cancel
echo.
echo Release build cancelled. Gradle was not started.
echo.
pause

:end
endlocal
