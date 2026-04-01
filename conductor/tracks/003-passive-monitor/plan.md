# Track 003-passive-monitor: Passive Monitoring

## Goal
Implement the battery-efficient background heart rate monitoring using `PassiveMonitoringClient`.

## Tasks
1.  **Request Permissions:**
    -   Ensure `BODY_SENSORS` and `BODY_SENSORS_BACKGROUND` are granted.
2.  **Configuration:**
    -   Setup `PassiveListenerConfig` for `DataType.HEART_RATE_BPM` and `DataType.USER_ACTIVITY_STATE`.
3.  **Broadcast Receiver:**
    -   Implement a `BroadcastReceiver` or use a `PassiveListenerService` to receive health data updates.
4.  **Anomaly Detection:**
    -   If activity is `PASSIVE` and `HR > threshold`, trigger the "Watching Closer" transition.
