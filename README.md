# Namaz Tracker

A simple and elegant Android application to track your daily prayers (Salah), view your progress, and stay consistent with your spiritual goals.

## Features

- **Prayer Tracking:** Easily mark your prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) as completed or skipped.
- **Visual Insights:** View your prayer statistics with breakdown charts and streak tracking (current and best).
- **Qibla Finder:** Built-in compass to help you find the Qibla direction.
- **Customizable Themes:** Supports Light, Dark, and System Default themes using Material 3.
- **Smart Notifications:** Receive reminders to keep your tracking up to date.
- **Privacy First:** Your data stays on your device using a local Room database.

## Tech Stack

- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Architecture:** MVVM (Model-View-ViewModel).
- **Database:** [Room](https://developer.android.com/training/data-storage/room) for local persistence.
- **Navigation:** [Jetpack Navigation](https://developer.android.com/guide/navigation/navigation-getting-started) for Compose.
- **Lifecycle:** [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines) and Flow for reactive data handling.

## Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK 34+
- A device or emulator running Android 8.0 (API 26) or higher

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Ayaanh001/Namaz-Tracker.git
   ```
2. Open the project in Android Studio.
3. Sync Project with Gradle Files.
4. Run the app on your device/emulator.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Icons by Google Material Icons.
- Inspired by the need for a simple, ad-free prayer tracker.
