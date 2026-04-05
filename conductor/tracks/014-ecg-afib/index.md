# Track 014: ECG & AFib

## Overview
Integrate irregular rhythm detection during passive monitoring and provide UI prompts for manual ECG verification.

## Objectives
- Monitor for irregular heart rhythms in `PassiveMonitoringService`.
- Implement user notification logic to prompt for an ECG.
- Integrate with `HealthServicesClient` ECG data types (if supported on target hardware).
- Provide a clear call-to-action in the mobile and wear apps.

## Key Files
- `wear/src/main/java/com/heart/sense/wear/service/PassiveMonitoringService.kt`
- `mobile/src/main/java/com/heart/sense/service/AlertListenerService.kt`
