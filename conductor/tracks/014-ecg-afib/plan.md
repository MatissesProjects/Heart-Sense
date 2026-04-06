# Plan: Track 014 - ECG & AFib

## Phase 1: Detection Logic (Wear)
- [x] **Task 1.1:** Create `RhythmEvaluator.kt` to analyze RR-interval variability.
- [x] **Task 1.2:** Update `PassiveMonitoringService` to use `RhythmEvaluator` when stationary.
- [x] **Task 1.3:** Add `PATH_IRREGULAR_RHYTHM` to `Constants.kt`.

## Phase 2: User Prompting (Wear)
- [x] **Task 2.1:** Implement a specialized notification in `PassiveMonitoringService` for irregular rhythms.
- [x] **Task 2.2:** Add a "Launch ECG" button to the notification that triggers `com.fitbit.ecg`.

## Phase 3: Cross-Device Alerting (Mobile)
- [x] **Task 3.1:** Add `/irregular_rhythm` listener to `AlertListenerService`.
- [x] **Task 3.2:** Implement mobile notification for irregular rhythm with guidance.
