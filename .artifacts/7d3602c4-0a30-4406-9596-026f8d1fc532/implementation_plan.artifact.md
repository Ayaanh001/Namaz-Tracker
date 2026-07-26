# Fix Qibla Direction Accuracy and Location Handling

Improve the Qibla finder's accuracy and reliability, especially when location services are disabled, by implementing location caching and magnetic declination correction.

## Proposed Changes

### Data Layer

#### [MODIFY] [SettingsManager.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/data/SettingsManager.kt)
- Add `LAST_LATITUDE` and `LAST_LONGITUDE` to preferences.
- Add methods to save and retrieve the last known location coordinates.

### UI/Logic Layer

#### [MODIFY] [QiblaViewModel.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/QiblaViewModel.kt)
- **Location Caching**:
    - Load the last saved location from `SettingsManager` on initialization.
    - Attempt to get the current location using `fusedLocationClient.getCurrentLocation`.
    - Fallback to `fusedLocationClient.lastLocation` if current location is unavailable.
    - If both fail, use the cached location from `SettingsManager`.
    - Save any successfully retrieved location to `SettingsManager` for future use.
- **Accuracy Improvement**:
    - Implement magnetic declination correction using `GeomagneticField`.
    - Update the `bearing` in `uiState` to reflect True North instead of Magnetic North.
    - Ensure `qiblaDirection` is calculated even if only a cached location is available.
- **State Management**:
    - Update `QiblaState` to indicate if the location is "live" or "cached" (optional, but helpful for UX).

#### [MODIFY] [QiblaScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/QiblaScreen.kt)
- Ensure the UI correctly displays the Qibla direction and distance when a cached location is used.
- (Optional) Add a "Location services off" warning if using cached data.

## Verification Plan

### Automated Tests
- N/A (UI and sensor-heavy logic is best verified manually).

### Manual Verification
1.  **With Location ON**: Open the Qibla screen, verify it detects location and shows direction/distance.
2.  **Toggle Location OFF**:
    - Close the app, turn off GPS/Location.
    - Open the app, verify it still shows the Qibla direction based on the last known location.
    - Verify the "Detecting location..." state transitions to showing the cached location.
3.  **Accuracy Check**: Compare with a known accurate Qibla app (like Muslim Pro or similar) to ensure the needle points in the same direction, especially considering declination.
