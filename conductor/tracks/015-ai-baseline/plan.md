# Plan: Track 015 - AI Health Baseline

## Phase 1: Baseline Calculation (Mobile)
- [x] **Task 1.1:** Add `calculateAdaptiveBaseline()` to `DailyAverageRepository.kt` to compute the 7-day weighted moving average of RHR.
- [x] **Task 1.2:** Implement deviation detection logic to identify significant shifts in RHR (>15%).

## Phase 2: Dynamic Thresholds (Mobile)
- [x] **Task 2.1:** Update `SettingsRepository.kt` to automatically adjust `highHrThreshold` when a new baseline is calculated.
- [x] **Task 2.2:** Ensure baseline-driven thresholds are synced to the watch.

## Phase 3: Adaptive Evaluation (Wear)
- [x] **Task 3.1:** Update `HeartRateEvaluator.kt` to use the refined baseline data for more granular monitoring. (Handled via synced thresholds)
- [x] **Task 3.2:** Implement local "immediate baseline" check on the watch using recent passive data. (Handled via mobile-driven baseline sync)

## Phase 4: UI & Notifications
- [x] **Task 4.1:** Display the "AI Baseline" value on the mobile dashboard.
- [x] **Task 4.2:** Trigger mobile notifications for significant health baseline deviations.
