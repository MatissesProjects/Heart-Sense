# Track 001-setup: Project Setup

## Goal
Initialize the Android project with mobile and WearOS modules and configure essential permissions.

## Tasks
1.  **Project Initialization:**
    -   Create Android Studio project with `mobile` (app) and `wear` (wear-app) modules.
    -   Use Kotlin DSL for Gradle.
2.  **Manifest Configuration:**
    -   Add `BODY_SENSORS`, `BODY_SENSORS_BACKGROUND`, `ACTIVITY_RECOGNITION`, `FOREGROUND_SERVICE_HEALTH`, and `POST_NOTIFICATIONS` to both modules as needed.
3.  **Dependency Setup:**
    -   Add `androidx.health:health-services-client`.
    -   Add `com.google.android.gms:play-services-wearable`.
    -   Add Hilt dependencies.
4.  **Base UI:**
    -   Setup basic Jetpack Compose themes for both mobile and wear.
