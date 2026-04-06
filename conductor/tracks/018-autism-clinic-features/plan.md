# Implementation Plan: Track 018 - Autism Clinic Integration

## Overview
This track adds features to detect and record stress events, allowing for better tracking and intervention in an Autism clinic setting.

---

## 🏗 Sub-tasks

### 1. Stress Detection Algorithm
-   [ ] Research optimal HR/HRV delta for stress detection in neurodivergent individuals.
-   [ ] Implement a `StressEvaluator` on the watch to complement `IllnessEvaluator`.
-   [ ] Test the evaluator with synthetic data representing rapid HR spikes.

### 2. Event Tagging System
-   [ ] Update `Alert` data class in the `mobile` module to include an optional `tag: String` field.
-   [ ] Update `AlertDao` and `AlertsRepository` to handle the new field.
-   [ ] Add a UI component to the `Alert` notification and Dashboard to allow tagging.

### 3. Baseline Calibration Tool
-   [ ] Create a `CalibrationViewModel` and screen in the `mobile` app.
-   [ ] Implement a "Start Calibration" command from phone to watch.
-   [ ] On the watch, record 5 minutes of resting HR/HRV to set a "Calm Baseline".

### 4. Caregiver/Clinician Notification Bridge
-   [ ] Integrate with a cloud-based notification service (e.g., Firebase) to forward high-priority alerts to secondary devices.
-   [ ] Implement a "Silent Alert" mode that notifies caregivers without alarming the user.

---

## ✅ Verification Strategy
-   **Unit Tests:** Test the `StressEvaluator` with various HR/HRV scenarios.
-   **Integration Tests:** Verify that tags selected on the phone are correctly saved to the database.
-   **Manual Testing:** Perform a "Mock Stress Event" by artificially raising HR (e.g., light exercise) and verifying the alert and tagging flow.
