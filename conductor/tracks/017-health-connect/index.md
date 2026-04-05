# Track 017: Health Connect

## Overview
Enable bi-directional synchronization with Google Health Connect to share data with the broader Android health ecosystem.

## Objectives
- Integrate the `androidx.health.connect:connect-client` library.
- Request permissions for writing HR, RR, and potentially HRV data.
- Map Heart-Sense measurements to Health Connect data types.
- Implement background sync to push high-resolution anomalies to the ecosystem.

## Key Files
- `mobile/build.gradle.kts`
- `mobile/src/main/java/com/heart/sense/data/HealthConnectRepository.kt` (New)
- `mobile/src/main/java/com/heart/sense/data/DailyAverageRepository.kt`
