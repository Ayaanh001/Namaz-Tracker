# Implementation Plan - Chat-style Popover for Stats Details

The user wants to replace the standard `AlertDialog` with a more integrated "chat-like" bubble (popover) that points an arrow directly at the clicked grid box. This creates a more contextual and visually interesting experience.

## Proposed Changes

### [UI] Stats Screen Popover

#### [MODIFY] [StatsScreen.kt](file:///C:/Users/Ah/AndroidStudioProjects/NamazTracker/app/src/main/java/com/hussain/namaztracker/ui/screens/StatsScreen.kt)

**1. Coordinate Tracking**
- Modify `PrayerStatusBox` to accept an `onGloballyPositioned` callback or simply calculate its center coordinates when clicked.
- Store the `LayoutCoordinates` or the absolute `Offset` of the clicked box in the `selectedDetail` state.

**2. Custom Speech Bubble Shape**
- Create a `SpeechBubbleShape` that draws a rounded rectangle with a triangular "arrow" at the bottom.
- **Dynamic Arrow**: The arrow should ideally point to the anchor point, even if the bubble itself is shifted horizontally to stay on screen.

**3. `StatsDetailPopover` (New Component)**
- Use the `Popup` composable to display the detail view.
- **Positioning**: Use a custom `PopupPositionProvider` to place the bubble above the clicked grid box, centered horizontally relative to the box.
- **Design**:
    - Maintain the "Premium" look: status badge, prayer icon, and personalized message.
    - Add a slight "bounce" or "scale-in" entry animation for that "sick" feel.
    - Dismiss the popover when tapping outside or tapping the box again.

**4. Transition**
- Remove the `AlertDialog` and `StatsDetailDialog` (or refactor `StatsDetailDialog` into the popover content).

## Verification Plan

### Manual Verification
1. Open the Stats tab.
2. Click various boxes in the grid (left, middle, and right of the screen).
3. Verify that:
    - The bubble appears above the box.
    - The arrow points exactly at the box.
    - The bubble doesn't go off the screen edges (clipping/shifting logic).
    - The information inside is legible and well-designed.
4. Test clicking outside the bubble to dismiss it.
