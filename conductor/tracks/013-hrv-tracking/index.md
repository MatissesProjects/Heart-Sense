# Track 013: HRV Analytics

## Overview
Implement Heart Rate Variability (HRV) tracking based on RMSSD (Root Mean Square of Successive Differences) during sleep states.

## Objectives
- Extract RR-interval data from overnight measurements.
- Implement RMSSD calculation logic in `OvernightDataRepository`.
- Sync HRV scores to the phone dashboard.
- Visualize HRV trends to provide recovery insights.

## Key Files
- `wear/src/main/java/com/heart/sense/wear/data/OvernightDataRepository.kt`
- `mobile/src/main/java/com/heart/sense/data/DailyAverage.kt`
- `mobile/src/main/java/com/heart/sense/ui/dashboard/HealthDashboard.kt`
