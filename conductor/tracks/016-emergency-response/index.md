# Track 016: Emergency Response

## Overview
Implement automated escalation for critical heart rate events that remain unacknowledged.

## Objectives
- Allow users to store Emergency Contacts in `SettingsDataStore`.
- Implement an "Emergency Countdown" UI for critical alerts.
- Automatically notify contacts via SMS or simulated API if the user doesn't respond.
- Include high-resolution data snippet and location in the notification.

## Key Files
- `mobile/src/main/java/com/heart/sense/data/SettingsDataStore.kt`
- `mobile/src/main/java/com/heart/sense/service/AlertListenerService.kt`
- `mobile/src/main/java/com/heart/sense/MainActivity.kt`
