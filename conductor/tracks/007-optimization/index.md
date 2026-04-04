# Track 007: Pipeline Optimization & Refactoring

## Summary
Consolidate heart rate monitoring logic, improve battery efficiency by coordinating background services, and optimize cross-device communication.

## Goals
-   [x] Consolidate threshold calculation logic into the `Settings` model.
-   [x] Implement a unified `HeartRateEvaluator` for standardized data processing.
-   [x] Coordinate `PassiveMonitoringService` and `HealthMonitoringService` to prevent redundant processing.
-   [x] Implement Resilient Monitoring (survive reboots).
-   [x] Optimize message routing using `CapabilityClient` to target the phone node specifically.

## Status
- **Phase:** Implementation
- **Progress:** 100%
