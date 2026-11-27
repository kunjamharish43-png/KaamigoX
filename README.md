# Kaamigo X — Android utility app

Orange + white Android app starter built with Kotlin and Jetpack Compose. The home screen contains a searchable, category-filtered catalogue of 100 proposed tools.

## Current implementation status
- Working UI: home dashboard, search, category filters, Hindi/English labels, tool detail navigation.
- Implemented starter actions: word/character counter, case converter, password/PIN generator, text reverser, extra-space cleaner, line sorter.
- The remaining catalogue entries are clearly marked as planned modules; they are not falsely presented as fully implemented.
- Start.io SDK dependency and App ID manifest placeholder are included. Banner rendering is intentionally disabled until you add your real App ID and verify the SDK integration.
- Offline-first: implemented utility actions run locally. No online AI service is included.

## Open in Android Studio
1. Download/extract this repository.
2. Open the `KaamigoX` folder in Android Studio (JDK 17 recommended).
3. Let Gradle sync.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).

## Configure Start.io
1. Create/register your app in the Start.io publisher portal and copy its App ID.
2. In `gradle.properties` add: `STARTIO_APP_ID=YOUR_REAL_APP_ID`
3. In `app/src/main/java/com/kaamigox/app/MainActivity.kt`, change `BuildConfigField.isConfigured()` to return `true` only after you have configured and tested your ID.
4. Build and test with Start.io's current integration guide. Do not click your own live ads. Use the provider's test procedures where available.
5. Review consent/privacy requirements and your target audience before release.

SDK dependency currently pinned to `com.startapp:inapp-sdk:5.3.1` based on Start.io's published Maven integration documentation. Check their docs for updates before publishing.

## GitHub Actions
Push to `main` or run the workflow manually. The workflow builds a debug APK and uploads it as an artifact. It does not publish to Google Play.

## Important
This is a starter source project, not a verified release APK. Build and test it on Android Studio/physical devices; complete each planned tool, privacy disclosures, permissions, accessibility, localization, and store compliance before release.
