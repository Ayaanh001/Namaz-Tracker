# Redesign Date Pill UI

This plan updates the `DatePill` component to match the requested design: a split-background "nested" look with a selection indicator.

## Proposed Changes

### [Salah Screen]

#### [MODIFY] [SalahScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/SalahScreen.kt)
- **Restructure `DatePill`**:
    - Use a main `Column` container with a large rounded corner radius.
    - Split the container into two sections:
        - **Top Section**: Displays the day abbreviation (e.g., "Fri") with a lighter background.
        - **Bottom Section**: A nested rounded container displaying the date number (e.g., "17") with a darker/solid background.
- **Theme Awareness**:
    - **Selected State**: Use `primary` (Maroon) with a lighter top and darker bottom.
    - **Unselected State**: Use `surfaceVariant` (Beige/Gray) with a lighter top and darker bottom.
- **Selection Indicator**: Add a small, centered pill/dot at the bottom of the `DatePill` when it is selected.
- **Adjust Dimensions**: Refine width and height to match the "capsule" proportions in the reference image.

## Verification Plan

### Manual Verification
- Deploy to an Android device.
- Verify the "nested" look of the date pills (two-tone background).
- Check the selection indicator (dot) appears correctly for the selected date.
- Verify the design works in both light and dark modes (using Material 3 color mappings).
- Ensure the layout remains stable (no jumping) when selecting dates.
