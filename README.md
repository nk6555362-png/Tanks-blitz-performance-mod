# Tanks Blitz Performance Mod — MVP 0.1

Android companion app for Realme 15 Pro 5G / Android 16, no root.

## Current MVP
- Minimal dark UI.
- User-enabled floating overlay.
- Battery temperature (not SoC/GPU hotspot temperature).
- Android thermal status.
- System RAM usage.
- FPS is deliberately shown as `N/A`: normal third-party apps cannot reliably read another app's FPS on stock Android.

## Build
1. Install Android Studio and Android SDK Platform 35.
2. Open this folder in Android Studio.
3. Use JDK 17.
4. Allow Gradle sync to finish.
5. Build > Build Bundle(s) / APK(s) > Build APK(s).

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Permissions and limitations
- Overlay permission is requested by the app when you enable the monitor.
- Android may require notification permission for the foreground-service notification.
- Android/OEM battery management may stop the service; allow background activity if the system offers that option.
- No CPU/GPU overclocking, no game-file modification, no anti-cheat interaction.
- The 120 FPS value is a target only. Actual available frame rates depend on the game build, display mode, device policy and thermal limits.

## Next development steps
1. Verify build and overlay behavior on the target phone.
2. Add a user-configurable game launch shortcut after confirming the installed Tanks Blitz package name.
3. Add a measurement workflow for frame-time/FPS using supported Android developer/performance tools where available.
4. Add persistent profiles and safe settings guidance.

## GitHub Actions
The repository includes `.github/workflows/android.yml`.
Push to `main` (or open a pull request) to build the debug APK. You can also run it manually from the Actions tab using **Android APK → Run workflow**. Download the APK from the run's **Artifacts** section.
