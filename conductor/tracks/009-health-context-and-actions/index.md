# Track 009: Health Context & Advanced Actions

## Summary
Incorporate more granular health metrics (Respiratory Rate, Sleep Stages) into monitoring logic and expand notification capabilities with actions like Snooze, False Positive reporting, and Emergency Contact integration.

## Goals
-   [x] Implement a "Snooze" state to temporarily silence alerts.
-   [x] Add "False Positive - Exercising" action to refine monitoring accuracy.
-   [x] Integrate "Emergency Contact" action for critical heart rate alerts.
-   [x] Implement "Baseline Calibration" phase for personalized thresholds.
-   [ ] Expand `HeartRateEvaluator` to accept and process additional health metrics.

## Research Questions
- [x] How can we implement a 'Snooze' state in `SettingsDataStore` that automatically expires after a set duration, and how should `HeartRateEvaluator` handle this state to prevent duplicate notifications while snoozed?
- [x] How can we retrieve Respiratory Rate and Sleep Stage data from Health Services in background/passive mode?
- [x] How can we implement a 'Baseline Calibration' phase where the app records the user's Resting Heart Rate and average Respiratory Rate for the first 24-48 hours, storing these values in `SettingsDataStore` to provide truly personalized thresholds?
- [ ] How should we store and manage the 'Emergency Contact' phone number? Should it be in the `Settings` model (synced to watch) or kept purely on the phone in a separate `SecureDataStore`?
- [x] Identify the best way to implement ACTION_FALSE_POSITIVE and ACTION_EMERGENCY in the notification flow. Should these actions trigger a broadcast back to the watch to stop monitoring?
- [ ] How can we design a 'Calibration Progress' UI for both the phone and watch that shows the user how many hours of data have been collected and provides an estimated completion time?

## Status
- **Phase:** Implementation
- **Progress:** 80%
