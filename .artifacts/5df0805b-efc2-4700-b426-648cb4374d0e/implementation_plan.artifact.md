# Implementation Plan - Add Qibla Tab

This plan details the steps to add a new "Qibla" tab as the third item in the navigation bar. The Qibla screen will display the direction of the Kaaba prominently using device sensors and location data.

## User Review Required

> [!IMPORTANT]
> This feature requires **Location Permission** (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION) to calculate the Qibla direction from the user's current position.

## Proposed Changes

### Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/gradle/libs.versions.toml)
- Add `play-services-location` dependency for efficient location retrieval.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/build.gradle.kts)
- Include `play-services-location` in dependencies.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`
- Add `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`

### Navigation

#### [MODIFY] [Screen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/Screen.kt)
- Add `Screen.Qibla` object with a compass/explore icon.
- Reorder `bottomNavItems` to: `Salah`, `Stats`, `Qibla`, `Settings`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/MainActivity.kt)
- Add `composable(Screen.Qibla.route) { QiblaScreen() }` to the `NavHost`.

### Features

#### [NEW] [QiblaViewModel.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/QiblaViewModel.kt)
- Manage location updates using `FusedLocationProviderClient`.
- Manage device orientation updates using `SensorManager` (Magnetometer & Accelerometer).
- Calculate Qibla bearing and distance to Mecca.
- Expose a `StateFlow` with the current heading, qibla angle, and distance.

#### [NEW] [QiblaScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/QiblaScreen.kt)
- UI implementation:
    - Handle permission request.
    - Large compass needle/circle showing the direction.
    - Prominent degree indication.
    - Two concise lines below: Distance to Mecca and current city/coordinates (if available).

## Verification Plan

### Automated Tests
- Unit tests for Qibla calculation logic (e.g., verify bearing from London, NYC, or Dubai to Mecca).

### Manual Verification
- Deploy to device/emulator.
- Verify bottom navigation works and Qibla is in the 3rd position.
- Grant location permission.
- Verify the compass needle moves as the device rotates (on real device).
- Verify distance and degrees are displayed correctly.
