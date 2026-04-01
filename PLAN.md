# Heart-Sense Implementation Plan

## Overview
Heart-Sense is a two-tiered health monitoring system for Pixel Watch 3 and Pixel 9. It balances battery efficiency with real-time accuracy to detect heart rate anomalies and provide timely alerts.

---

## 🚀 Key Features & Requirements

### 1. Two-Tiered Monitoring (WearOS)
-   **Passive Mode:** Uses `PassiveMonitoringClient` for battery-efficient background tracking.
-   **Watching Closer Mode:** Shifts to `MeasureClient` and a Foreground Service for 1Hz real-time tracking when anomalies are detected.

### 2. Configuration & Sync (Mobile to Watch)
-   **Threshold Management:** Set HR limits (e.g., 100bpm stationary) from the Pixel 9.
-   **Data Sync:** Uses `DataClient` to sync thresholds and health state to the watch instantly.

### 3. Health Context (Sick Mode)
-   **Mark State:** Users can mark themselves as "sick" or "resting" from the phone or watch.
-   **Dynamic Triggers:**
    -   **Lower Thresholds:** Alerts trigger at lower HR levels when sick (e.g., alert at 90bpm instead of 100bpm).
    -   **Sit-Down Warnings:** Proactive warnings if the user is active while marked as "sick" or if HR is elevated while stationary.

### 4. Cross-Device Alerts
-   **Message System:** Watch sends `/hr_alert` via `MessageClient`.
-   **Phone Response:** Pixel 9 triggers high-priority notifications/alarms.

---

## 🛠 Tech Stack
-   **Kotlin & Coroutines/Flows**
-   **Jetpack Compose** (Mobile & WearOS)
-   **Health Services API**
-   **Wearable Data Layer API**
-   **Hilt** (DI)

---

## 🛤 Tracks (Conductor)
Detailed progress is tracked in the `conductor/` directory.

1.  **[001-setup](./conductor/tracks/001-setup/plan.md):** Project Setup & Permissions.
2.  **[002-data-sync](./conductor/tracks/002-data-sync/plan.md):** Configuration & DataLayer Sync.
3.  **[003-passive-monitor](./conductor/tracks/003-passive-monitor/plan.md):** Background Passive Monitoring.
4.  **[004-active-monitor](./conductor/tracks/004-active-monitor/plan.md):** "Watching Closer" Foreground Service.
5.  **[005-alerts](./conductor/tracks/005-alerts/plan.md):** UI & Cross-Device Notification System.
6.  **[006-sick-mode](./conductor/tracks/006-sick-mode/plan.md):** Logic for Health Context & Dynamic Thresholds.

---

## 📜 Android Permissions
-   `BODY_SENSORS`
-   `BODY_SENSORS_BACKGROUND`
-   `ACTIVITY_RECOGNITION`
-   `FOREGROUND_SERVICE_HEALTH`
-   `POST_NOTIFICATIONS`
