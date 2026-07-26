# Refactor Prayer Completion Bottom Sheet

The goal is to move the bottom sheet logic and supporting models into separate files to improve the modularity and maintainability of the codebase. `SalahScreen.kt` is currently handling too many responsibilities.

## Proposed Changes

### [Models]

#### [NEW] [Prayer.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/models/Prayer.kt)
- Move `PrayerStatus` enum and `PrayerEntry` data class to this file.
- This allows these models to be shared across different screens (e.g., StatsScreen in the future).

### [UI Components]

#### [NEW] [PrayerCompletionBottomSheet.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/components/PrayerCompletionBottomSheet.kt)
- Move `PrayerCompletionBottomSheet` and `CompletionOptionItem` composables to this file.
- This keeps the screen-level logic in `SalahScreen` separate from the specific implementation details of the bottom sheet.

### [Screens]

#### [MODIFY] [SalahScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/SalahScreen.kt)
- Remove the moved models and components.
- Add necessary imports.

## Verification Plan

### Automated Tests
- Run `gradle build` to ensure all references are correctly updated.

### Manual Verification
- Deploy the app and verify that the bottom sheet still functions correctly.
