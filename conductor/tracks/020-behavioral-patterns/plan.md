# Implementation Plan: Track 020 - Behavioral Pattern Recognition

## Overview
This track focuses on recognizing complex behaviors like pacing or sudden standing/agitation by combining activity state transitions and motion data.

---

## 🏗 Sub-tasks

### 1. Pacing Detection
-   [ ] Research typical pacing behaviors and their frequency in an Autism clinic context.
-   [ ] Use step count deltas and orientation changes (gyroscope) to detect repetitive back-and-forth movement.
-   [ ] Implement a `PacingDetector` on the watch.

### 2. Sudden Agitation Alerts
-   [ ] Combine heart rate spikes with a sudden transition to a "walking" activity state.
-   [ ] Implement a `SuddenAgitation` trigger in the `PassiveMonitoringService`.
-   [ ] Trigger a "Calming Intervention" notification when these conditions are met.

### 3. Custom Behavioral Rules
-   [ ] Update the `Settings` data class and DataStore to include behavioral detection rules (e.g., "Alert on sudden movement", "Detect pacing").
-   [ ] Add UI to the phone dashboard for clinicians to toggle these behavioral rules.

### 4. Behavioral Alert UI
-   [ ] Add "Behavioral Alert" types (Pacing, Agitation) to the `AlertsRepository`.
-   [ ] Create unique icons and notification sounds for these behavioral alerts.

---

## ✅ Verification Strategy
-   **Unit Tests:** Verify the `PacingDetector` logic with simulated walk-turn-walk sequences.
-   **Integration Tests:** Ensure that toggling behavioral rules on the phone correctly updates the watch's behavior.
-   **Manual Testing:** Wear the watch and perform pacing (walking back and forth) and sudden standing to verify detection.
