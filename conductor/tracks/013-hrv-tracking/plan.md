# Plan: HRV Analytics

## Phase 1: Data Collection (Wear)
- [x] **Task 1.1:** Add `rrIntervals` field to `OvernightMeasurement` Room entity.
- [x] **Task 1.2:** Update `PassiveMonitoringService` to extract RR intervals from `DataType.HEART_RATE_BPM` metadata.
- [x] **Task 1.3:** Implement RMSSD calculation in `OvernightDataRepository`.

## Phase 2: Synchronization
- [x] **Task 2.1:** Update sync serialization to include RR-interval data.
- [x] **Task 2.2:** Update `SyncListenerService` on mobile to parse incoming RR intervals.

## Phase 3: Visualization (Mobile)
- [x] **Task 3.1:** Add `hrvRmssd` to `DailyAverage` data model.
- [x] **Task 3.2:** Update `DailyAverageRepository` to calculate RMSSD for the dashboard.
- [x] **Task 3.3:** Add HRV line chart and summary column to `HealthDashboard.kt`.
