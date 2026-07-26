# Implementation Plan - Stats Time Range Switcher

This plan covers adding a tab switcher to the Stats screen to filter the completion breakdown by different time ranges (Weeks, Months, Years, All time).

## Proposed Changes

### [Models]

#### [NEW] [StatsRange.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/models/StatsRange.kt)
- Define `StatsRange` enum with options: `WEEKS`, `MONTHS`, `YEARS`, `ALL_TIME`.

### [SalahViewModel]

#### [MODIFY] [SalahViewModel.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/SalahViewModel.kt)
- Add `selectedRange` StateFlow (defaulting to `ALL_TIME`).
- Update `StatsInsights` to have a generic `breakdown` map.
- Modify `calculateInsights` to take the selected range into account when calculating the breakdown.
    - `WEEKS`: Last 7 days.
    - `MONTHS`: Last 30 days.
    - `YEARS`: Last 365 days.
    - `ALL_TIME`: All available records.

### [Stats Screen]

#### [MODIFY] [StatsScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/StatsScreen.kt)
- Add a new `TimeRangeSwitcher` composable based on the provided card-style design (rounded 50.dp, horizontal arrangement).
- Place the `TimeRangeSwitcher` above the `PremiumBreakdownCard`.
- Update the `PremiumBreakdownCard` title and data dynamically based on the selected range.
- Default the selection to `ALL_TIME`.

## Verification Plan

### Automated Tests
- Verify build with `./gradlew assembleDebug`.

### Manual Verification
- Open Stats screen.
- Toggle between Weeks, Months, Years, and All Time.
- Verify the Segmented Bar and percentages update correctly according to the selected range.
- Ensure the tab switcher animation/selection looks as requested and defaults to All Time.
