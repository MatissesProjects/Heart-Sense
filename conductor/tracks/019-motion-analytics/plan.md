# Implementation Plan: Track 019 - Motion & Fidgeting Analytics

## Overview
This track adds the ability to track hand movements and repetitive fidgeting patterns using the watch's onboard accelerometer and gyroscope.

---

## 🏗 Sub-tasks

### 1. Motion Data Acquisition
-   [ ] Implement a `MotionSensorRepository` using the standard Android `SensorManager`.
-   [ ] Request necessary permissions for `BODY_SENSORS` (if required for raw IMU).
-   [ ] Implement a high-pass filter to remove gravity components from accelerometer data.

### 2. Fidgeting Detection Logic
-   [ ] Implement a `FidgetDetector` that monitors the standard deviation and frequency of movement on the wrist.
-   [ ] Create a heuristic for "Stimming" detection based on repetitive motions within a specific frequency band (e.g., 1-5 Hz).
-   [ ] Distinguish between common activities (typing, walking) and fidgeting.

### 3. Data Integration
-   [ ] Update the `OvernightMeasurement` or create a new `BehavioralMeasurement` Room entity to store fidgeting intensity.
-   [ ] Update the `WearableCommunicationRepository` to sync motion intensity scores to the phone.

### 4. Correlation Engine
-   [ ] Create a component on the phone dashboard that overlays fidgeting intensity with heart rate spikes.
-   [ ] Allow clinicians to see if fidgeting precedes, accompanies, or follows a heart rate alert.

---

## ✅ Verification Strategy
-   **Unit Tests:** Test the `FidgetDetector` with synthetic motion data representing repetitive shaking vs. steady walking.
-   **Integration Tests:** Verify that motion scores are correctly synced and stored on the phone.
-   **Manual Testing:** Wear the watch and perform various hand movements (shaking, typing, waving) to verify detection accuracy.
