# Tasks - Stats Time Range Switcher

- [x] Create `StatsRange.kt` enum
- [x] Update `SalahViewModel.kt`
    - [x] Add `selectedRange` StateFlow (default `ALL_TIME`)
    - [x] Update `calculateInsights` to handle `StatsRange`
- [x] Update `StatsScreen.kt`
    - [x] Implement `TimeRangeSwitcher` composable
    - [x] Integrate switcher above breakdown card
    - [x] Update breakdown card to use dynamic range data
- [x] Verify functionality and default state
