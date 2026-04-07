# Track 023: Local Caregiver Dashboard

## Overview
Implement real-time local monitoring for parents or clinicians without sending data to the cloud.

## Objectives
- Integrate **Google Nearby Connections API** for offline device-to-device communication.
- Implement a "Broadcaster" mode on the wearer's phone to stream HR and alerts.
- Implement a "Follower" mode on the caregiver's phone to discover and display metrics.
- Ensure data never leaves the local Wi-Fi or Bluetooth range.
- Implement a local pairing mechanism (e.g., Device Name matching).

## Key Files
- `mobile/src/main/java/com/heart/sense/data/LocalSyncRepository.kt` (New)
- `mobile/src/main/java/com/heart/sense/ui/caregiver/LocalDashboard.kt` (New)
