# Track 004-active-monitor: Watching Closer

- [Implementation Plan](./plan.md)

## Summary
Implement the high-resolution foreground monitoring using `MeasureClient` when an anomaly is detected on WearOS.

## Progress
- [x] Create HealthMonitoringService (Foreground Service)
- [x] Implement MeasureClient callback for 1Hz HR data
- [x] Implement transition logic from Passive to Active
- [x] Add stabilization detection to revert to Passive
