# Heart-Sense

**Heart-Sense** is a battery-efficient, intelligent heart health monitoring system designed specifically for the **Pixel Watch 3** (Wear OS) and **Pixel 9** (Android). It bridges the gap between passive health tracking and real-time medical awareness by using a unique two-tiered monitoring architecture.

## Vision
Most health apps either drain battery with constant monitoring or miss critical events with infrequent polling. **Heart-Sense** solves this by operating in a power-efficient background mode, only shifting into high-resolution "Watching Closer" mode when an anomaly is detected.

---

## Key Features

### 1. Two-Tiered Monitoring Architecture
- **Passive Tier:** Utilizes Android Health Services `PassiveMonitoringClient` to track Heart Rate (HR) and Respiratory Rate (RR) in the background with minimal battery impact.
- **Watching Closer Tier:** Automatically triggers a `Foreground Service` and `MeasureClient` for 1Hz real-time tracking when heart rate anomalies are detected stationary.

### 2. Intelligent Health Context (Sick Mode)
- **Dynamic Thresholds:** Users can toggle "Sick Mode" to lower alert thresholds (e.g., from 100 BPM down to 90 BPM), ensuring earlier detection when the body is already stressed.
- **Sit-Down Warnings:** Proactive alerts advising the user to rest if their heart rate remains elevated relative to their current activity level.

### 3. Cross-Device Synchronization
- **Real-Time Alerts:** High-HR events on the watch instantly trigger high-priority notifications and alarms on the paired Pixel 9.
- **Seamless Config:** Thresholds and settings updated on the mobile app are synced instantly to the watch via the Wearable Data Layer API.
- **Historical Dashboard:** Visualize HR and RR trends over time with a clean, Vico-powered charting interface on the mobile app.

### 4. Advanced Illness Detection
- **Overnight Analysis:** Automated processing of overnight metrics to detect deviations from the user's baseline, providing a "Morning Health Check" alert if early signs of illness are detected.

---

## Tech Stack

- **Languages:** Kotlin, Kotlin Coroutines, Flows
- **Mobile UI:** Jetpack Compose, Vico Charts
- **Wear OS UI:** Jetpack Compose for Wear OS
- **Health Data:** Android Health Services API (`PassiveMonitoringClient`, `MeasureClient`)
- **Persistence:** Room Database (Mobile & Wear), DataStore (Preferences)
- **Communication:** Wearable Data Layer API (`DataClient`, `MessageClient`, `CapabilityClient`)
- **Dependency Injection:** Hilt

---

## Architecture & Performance
- **MVVM Pattern:** Clean separation between UI, Business Logic, and Data.
- **Battery Optimization:** Suspends all non-essential background processing on the watch when real-time monitoring is active to maximize efficiency.
- **Serialized Sync:** Mobile-side `Channel`-based serialization for incoming data batches ensures database integrity and prevents UI lag during heavy sync operations.

---

## Future Roadmap (Next Steps)

Based on industry standards and emerging wearable technology, the following features are planned:

### 1. HRV (Heart Rate Variability) Tracking
- Implement standard HRV (RMSSD) calculations during sleep to provide deeper insights into recovery and stress levels, similar to "Recovery Scores" in high-end fitness trackers.

### 2. ECG & AFib Detection
- Integrate with on-device ECG sensors (where available) to prompt the user for a manual ECG reading if irregular rhythms are detected during passive monitoring.

### 3. AI-Driven Predictive Insights
- Use local Machine Learning (on-device) to build a personalized "Health Baseline" that adapts to the user's unique physiology, moving beyond static thresholds.

### 4. Emergency Contact Integration
- Automatically notify a designated emergency contact or healthcare provider with location data if a "Critical HR" event is detected and not acknowledged within a certain timeframe.

### 5. Google Health Connect Integration
- Sync Heart-Sense data with Google Health Connect to allow other health apps to benefit from our high-resolution anomaly data.

---

## Permissions Required
- `BODY_SENSORS` & `BODY_SENSORS_BACKGROUND`: To read HR/RR data.
- `ACTIVITY_RECOGNITION`: To detect if the user is stationary or active.
- `FOREGROUND_SERVICE_HEALTH`: For real-time monitoring.
- `POST_NOTIFICATIONS`: For alerts and status updates.

---

## Development Setup
1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Ensure you have a Wear OS 4+ device or emulator.
4. Build and run the `mobile` app on your phone and the `wear` app on your watch.
