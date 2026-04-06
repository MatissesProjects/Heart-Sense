# Heart-Sense: Autism Clinic Adaptation Guide

## Overview
This document outlines how the **Heart-Sense** application can be adapted to assist in tracking physiological metrics (Heart Rate, HRV) for individuals in an Autism clinic. The goal is to provide early detection of stress or sensory overstimulation and facilitate clinician-led interventions.

---

## 🧩 Core Architecture & Suitability
The existing **Heart-Sense** architecture is well-suited for this use case because of its:
1.  **Passive Monitoring:** Continuous, battery-efficient background tracking (via `PassiveMonitoringClient`).
2.  **Real-time Alerts:** The ability to transition to high-frequency (1Hz) tracking when anomalies are detected.
3.  **Cross-Device Sync:** Seamless communication between a wearable (Watch) and a mobile device (Phone/Tablet).
4.  **Health Context (Sick Mode):** A framework for dynamic thresholding based on the user's current state.

---

## 🚀 Proposed Adaptations for Autism Clinic

### 1. Stress & Overstimulation Detection (Repurposing "Illness Detection")
Instead of just overnight analysis for illness, the system can be tuned to detect **Stress Events** during the day.
-   **Mechanism:** Monitor rapid spikes in Heart Rate (HR) and drops in Heart Rate Variability (HRV) relative to the user's baseline.
-   **Baseline:** Use the existing `IllnessEvaluator` logic to establish a "Calm Baseline" and trigger alerts when deviations exceed a specific threshold (e.g., 20% increase in HR while stationary).

### 2. Event Tagging & Contextual Data
To help clinicians understand triggers, the application can allow users or caregivers to "tag" an alert.
-   **Implementation:** Modify the `AlertActionReceiver` on the mobile device to include buttons like:
    -   **"Sensory Overload"** (Loud noise, bright lights)
    -   **"Transition"** (Moving between activities)
    -   **"Anxiety"**
    -   **"Physical Activity"** (To filter out false positives)
-   **Data Storage:** Save these tags in the `AlertsRepository` alongside the physiological data for later review.

### 3. Clinician Dashboard & Longitudinal Trends
Clinicians need to see patterns over time to adjust support plans.
-   **Mechanism:** Leverage the `DailyAverageRepository` to sync aggregated daily metrics to a central dashboard.
-   **Key Metrics for Clinicians:**
    -   Frequency and timing of stress alerts.
    -   Average resting HR trends.
    -   Correlations between "Tagged Events" and physiological spikes.

### 4. Sensory-Friendly Interventions
Alerts should be non-intrusive and provide immediate, helpful suggestions.
-   **Implementation:** Customize the mobile notification to trigger specific actions:
    -   Prompting a **"Guided Breathing"** exercise on the watch.
    -   Suggesting a **"Break"** or moving to a **"Quiet Zone"**.
    -   Sending a silent notification to a **Caregiver/Clinician's device**.

### 5. Fidgeting & Hand Movement Analytics
Fidgeting (stimming) is often a precursor to or a coping mechanism for stress.
-   **Mechanism:** Use the watch's **Accelerometer** and **Gyroscope** to detect repetitive, high-frequency hand movements.
-   **Classification:** Distinguish between functional movements (walking, typing) and repetitive fidgeting patterns using on-device ML or heuristic filters (FFT analysis of motion data).
-   **Insights:** Correlate fidgeting intensity with Heart Rate spikes to identify which sensory environments trigger these behaviors.

### 6. Activity Transitions & Pacing
Sudden changes in physical activity can indicate agitation or a need for a change in environment.
-   **Mechanism:** Track transitions from `PASSIVE` to `WALKING` or `RUNNING` states.
-   **Pacing Detection:** Detect repetitive walking patterns (pacing) within a confined area using a combination of step counts and orientation changes.
-   **Proactive Support:** If pacing is detected alongside elevated HR, the system can suggest a calming activity before the behavior escalates.

---

## 🛠 Technical Implementation Roadmap

### Track 018: Autism Clinic Integration
1.  **Stress Detection Logic:** Enhance `PassiveMonitoringService` to evaluate stress in real-time using HR/HRV deltas.
2.  **Alert Tagging UI:** Add a tagging system to the mobile `HealthDashboard` and `AlertActionReceiver`.
3.  **Caregiver Notification System:** Implement a mechanism to forward high-priority stress alerts to a secondary device.
4.  **Baseline Calibration Tool:** Create a "Baseline Calibration" mode where the user stays calm for 5-10 minutes to set their specific "Stress Thresholds."

---

## 📜 Ethical & Privacy Considerations
-   **Data Ownership:** Ensure all health data is stored securely and shared only with authorized clinicians.
-   **User Agency:** The user should always have the ability to snooze or dismiss alerts to prevent "alert fatigue."
-   **Transparency:** Clearly explain to the user and caregivers how the "Stress Score" is calculated.
