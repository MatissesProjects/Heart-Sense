# Heart-Sense: Product Definition

## Vision
Heart-Sense is a battery-efficient, two-tiered heart rate monitoring system for WearOS (Pixel Watch 3) and Android (Pixel 9). It provides passive monitoring to detect anomalies and shifts into high-resolution "Watching Closer" mode when issues are detected, alerting the user to rest or seek attention.

## Core Features
1.  **Passive Monitoring:** Uses Health Services `PassiveMonitoringClient` to batch heart rate data and detect high HR while stationary.
2.  **Watching Closer Mode:** Shifts to `MeasureClient` for real-second data when an anomaly is detected.
3.  **Cross-Device Alerts:** Notifies the Pixel 9 when the watch detects a high-HR event.
4.  **Configurable Thresholds:** Users can set HR limits from the phone, which sync to the watch.
5.  **Health Context (Sick Mode):** Users can mark themselves as "sick," which lowers thresholds and triggers alerts more aggressively.
6.  **Sit-Down Warnings:** Proactive alerts suggesting the user sit down if HR is elevated while they are active or resting.
