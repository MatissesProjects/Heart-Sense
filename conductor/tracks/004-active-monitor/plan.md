# Track 004-active-monitor: Watching Closer

## Goal
Implement the high-resolution foreground monitoring using `MeasureClient` when an anomaly is detected.

## Tasks
1.  **Foreground Service:**
    -   Create a `HealthMonitoringService` with type `FOREGROUND_SERVICE_TYPE_HEALTH`.
    -   Setup a persistent notification.
2.  **Real-Time Monitoring:**
    -   Inside the service, use `MeasureClient.registerMeasureCallback(DataType.HEART_RATE_BPM)`.
3.  **Handoff Logic:**
    -   Transition from passive monitoring to this service.
    -   Stop the service and revert to passive monitoring if HR stabilizes.
