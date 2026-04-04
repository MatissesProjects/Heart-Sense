# Plan: Health Context & Advanced Actions

## Phase 1: Notification Action Infrastructure
- [x] **Task 1.1:** Add `ACTION_FALSE_POSITIVE`, `ACTION_EMERGENCY`, and `PATH_STOP_HMS` to `Constants.kt` (Both).
- [x] **Task 1.2:** Update `AlertActionReceiver.kt` to handle new actions and send `/stop_hms` to watch.
- [x] **Task 1.3:** Update `SettingsListenerService` (Wear) to handle `/stop_hms` and terminate `HealthMonitoringService`.
- [x] **Task 1.4:** Update `AlertListenerService.kt` to include these actions in notifications.

## Phase 2: Snooze Implementation
- [x] **Task 2.1:** Implement `snoozeUntil` timestamp in `SettingsDataStore`.
- [x] **Task 2.2:** Update `HeartRateEvaluator` to return `MonitoringAction.None` if current time is before `snoozeUntil`.

## Phase 3: Advanced Health Metrics & Calibration
- [x] **Task 3.1:** Implement "Baseline Calibration" storage in `SettingsDataStore`.
- [ ] **Task 3.2:** Update `HealthServicesRepository` to request Respiratory Rate and Sleep Stages.
- [ ] **Task 3.3:** Incorporate new metrics into `HeartRateEvaluator`.
