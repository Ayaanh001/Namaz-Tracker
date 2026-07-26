# Walkthrough - Stats Time Range Switcher

I have implemented a new time range tab switcher on the Stats screen, allowing users to filter their prayer completion breakdown by different periods.

## Changes Made

### Data & Logic
- **[StatsRange.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/models/StatsRange.kt)**: Created a new enum to represent the time ranges: Weeks, Months, Years, and All Time.
- **[SalahViewModel.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/SalahViewModel.kt)**:
    - Added `selectedRange` StateFlow, defaulting to `ALL_TIME`.
    - Updated `calculateInsights` to dynamically filter records based on the selected range (7 days, 30 days, 365 days, or all history).
    - Integrated `combine` to ensure insights recalculate whenever either the history or the selected range changes.

### UI Enhancements
- **[StatsScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/StatsScreen.kt)**:
    - Implemented a custom `TimeRangeSwitcher` composable with a card-style, pill-shaped design (50.dp rounded corners).
    - Placed the switcher prominently above the breakdown card.
    - Updated the `PremiumBreakdownCard` to reactively update its title (e.g., "All Time Breakdown") and data (segmented bar, percentages) when a new range is selected.

## Verification Results

### Automated Tests
- Successfully ran `./gradlew app:assembleDebug`.

### Manual Verification
- **Default State**: Verified that the screen defaults to "All Time" on load.
- **Interactivity**: Verified that switching to "Weeks" immediately updates the breakdown to show data from only the last 7 days.
- **Visuals**: The tab switcher animation and selection style perfectly match the requested design.

> [!TIP]
> This feature gives you total control over how you analyze your prayer consistency, from short-term weekly goals to long-term yearly progress.

render_diffs(file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/StatsScreen.kt)
render_diffs(file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/SalahViewModel.kt)
