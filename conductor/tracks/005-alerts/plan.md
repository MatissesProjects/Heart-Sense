# Track 005-alerts: Alerting System

## Goal
Implement the cross-device alert system using `MessageClient` and phone notifications.

## Tasks
1.  **Watch Alert:**
    -   Send `/hr_alert` message to the Pixel 9 when anomaly detected.
    -   Display local notification on the watch.
2.  **Mobile Alert Service:**
    -   Implement `WearableListenerService` on the Pixel 9.
3.  **High-Priority UI:**
    -   Trigger a full-screen intent or loud notification on the phone.
    -   Add buttons to acknowledge the alert or mark the current state (e.g., "I'm sick").
