# Track 010: Illness Detection

## Summary
Implement automated illness detection by analyzing overnight heart rate and respiratory rate deviations from established baselines.

## Goals
-   [ ] Implement Room database for persistent overnight data storage.
-   [ ] Detect sleep-end events to trigger morning health analysis.
-   [ ] Implement `IllnessEvaluator` logic with weighted risk scores.
-   [ ] Add rich `/illness_alert` notifications to the phone.

## Research Questions
- [x] How to detect the 'End of Sleep' event using `USER_ACTIVITY_STATE_AWAKE`?
- [x] How to aggregate the last 4-8 hours of heart rate and respiratory rate data?
- [x] Sliding window vs Database? (Decision: Room Database)

## Status
- **Phase:** Implementation
- **Progress:** 0%
